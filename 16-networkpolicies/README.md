# Kapitel 16: Grundlagen der NetworkPolicies
<!-- level: advanced -->

Ohne weitere Konfiguration erlaubt Kubernetes **jeglichen** Netzwerkverkehr
zwischen allen Pods – auch über Namespace-Grenzen hinweg. **NetworkPolicies**
schränken das gezielt ein, indem sie definieren, welcher Traffic zu/von Pods
mit bestimmten Labels erlaubt ist.

> NetworkPolicies werden nur wirksam durchgesetzt, wenn der
> Cluster-Netzwerk-Plugin (CNI) sie unterstützt. Bei OpenShift ist das mit
> dem Standard-SDN/OVN-Kubernetes der Fall.

## Ausgangslage: alles ist erlaubt

```bash
oc apply -f 16-networkpolicies/deployment.yaml
oc apply -f 16-networkpolicies/service.yaml
oc apply -f 16-networkpolicies/test-client-pod.yaml
oc rollout status deployment/demo
oc wait --for=condition=Ready pod/test-client --timeout=120s

# Welche NetworkPolicies gibt es bereits im Project?
oc get networkpolicies

oc exec test-client -- curl -s --retry 10 --retry-all-errors --retry-delay 1 -o /dev/null -w '%{http_code}\n' http://demo:8080/
# → 200, ohne jede NetworkPolicy
```

## Deny-All als Ausgangsbasis

Eine leere `podSelector: {}` in Kombination mit `policyTypes: [Ingress]`
wählt **alle** Pods im Project aus und lässt **keinen** eingehenden Traffic
mehr zu, solange keine weitere Regel dem widerspricht:

```yaml
spec:
  podSelector: {}
  policyTypes:
    - Ingress
```

```bash
oc apply -f 16-networkpolicies/networkpolicy-deny-all.yaml
oc get networkpolicy deny-all
```

❗ Der folgende Test zeigt nur dann einen Timeout, wenn im Project keine weitere Policy
allen Traffic innerhalb des Projects erlaubt (z. B. `allow-same-namespace`, wie in der
Developer Sandbox oder in manchen Project-Templates) – NetworkPolicies sind additiv (siehe
unten):

```bash
oc exec test-client -- curl -s -o /dev/null -w '%{http_code}\n' --max-time 3 http://demo:8080/
# → Timeout (Exit-Code 28), kein Zugriff mehr möglich
```

Ausgehender Traffic (Egress) ist von dieser Policy nicht betroffen – der
`test-client`-Pod kann also weiterhin Verbindungen initiieren, solange keine
Egress-Policy existiert.

## Gezielt Traffic erlauben

`networkpolicy-allow-demo.yaml` erlaubt eingehenden Traffic auf Port 8080 zu
Pods mit Label `app: demo`, aber **nur** von Pods mit Label
`app: test-client`:

```yaml
spec:
  podSelector:
    matchLabels:
      app: demo
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: test-client
      ports:
        - protocol: TCP
          port: 8080
```

```bash
oc apply -f 16-networkpolicies/networkpolicy-allow-demo.yaml

oc exec test-client -- curl -s --retry 10 --retry-all-errors --retry-delay 1 -o /dev/null -w '%{http_code}\n' http://demo:8080/
# → 200, wieder erreichbar
```

Damit koexistieren zwei Policies auf denselben Pod (`deny-all` und
`allow-demo-from-test-client`): NetworkPolicies sind additiv – es reicht,
dass **eine** passende Regel den Traffic erlaubt.

```bash
oc describe networkpolicy
```

## `podSelector` vs. `namespaceSelector`

In `ingress[].from` lässt sich neben `podSelector` auch `namespaceSelector`
angeben, um Traffic aus bestimmten Namespaces zu erlauben (z. B. von einem
Monitoring-Namespace). Da die Übungen in diesem Seminar bewusst ohne eigene
Namespaces auskommen, liegt der Fokus hier auf `podSelector` – in echten
Multi-Namespace-Setups ist `namespaceSelector` (oft kombiniert mit
`podSelector`) das gängigere Werkzeug, um Team- oder
Umgebungsgrenzen abzubilden.

## Egress

Analog zu `Ingress` lässt sich mit `policyTypes: [Egress]` auch ausgehender
Traffic einschränken (z. B. um zu verhindern, dass ein Pod beliebige externe
Adressen kontaktiert). Das Grundprinzip ist identisch: Eine leere
`podSelector: {}` mit `policyTypes: [Egress]` blockiert zunächst allen
ausgehenden Traffic, gezielte `egress`-Regeln erlauben dann wieder bestimmte
Ziele.

## Manifeste in diesem Kapitel

- `deployment.yaml` / `service.yaml` – Demo-App als Ziel des Traffics
- `test-client-pod.yaml` – einfacher Pod zum Testen von Verbindungen
- `networkpolicy-deny-all.yaml` – blockiert allen eingehenden Traffic
- `networkpolicy-allow-demo.yaml` – erlaubt Ingress zu `app: demo` nur vom Testclient

---

## Weiterführende Links

- [Kubernetes: NetworkPolicies](https://kubernetes.io/docs/concepts/services-networking/network-policies/)
- [OpenShift 4.22: Network Security](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/network_security/index)
- [OpenShift 4.22: Networking Overview](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/networking_overview/index)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
