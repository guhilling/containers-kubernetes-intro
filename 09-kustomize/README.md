# Kapitel 9: Kustomize Grundlagen
<!-- level: rookie -->

## Warum Kustomize?

Bisher haben wir Manifeste direkt mit `oc apply -f` angewendet. Sobald mehrere
Umgebungen (test, prod) mit leicht unterschiedlichen Werten (Image-Tag,
Replicas, Resources) verwaltet werden müssen, wird das schnell unübersichtlich
– Kopieren und manuelles Anpassen ist fehleranfällig.

**Kustomize** löst das **ohne Templating** (keine `{{ }}`-Platzhalter wie bei
Helm): Ein `base`-Verzeichnis enthält die gemeinsamen, "nackten" Manifeste.
`overlays` referenzieren die Base und beschreiben nur die **Unterschiede** als
Patches. Kustomize ist fest in `kubectl`/`oc` integriert (`-k`-Flag).

```
09-kustomize/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
└── overlays/
    ├── test/
    │   └── kustomization.yaml
    └── prod/
        ├── kustomization.yaml
        └── patch-resources.yaml
```

---

## Die Kustomization-Datei

Jedes Verzeichnis mit einer `kustomization.yaml` ist eine **Kustomization**.
Sie listet auf, welche Manifeste zusammengehören (`resources`) und welche
Transformationen angewendet werden.

```yaml
# base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

labels:
  - pairs:
      app: demo
    includeSelectors: true

resources:
  - deployment.yaml
  - service.yaml
```

`labels` (Nachfolger des älteren `commonLabels`) fügt allen Ressourcen ein
Label hinzu. Mit `includeSelectors: true` landet es auch in
`spec.selector`/`matchLabels` – so bleibt z. B. der Service-Selector konsistent
mit den Pod-Labels, ohne dass man es doppelt pflegen muss.

---

## Overlays: Image überschreiben

Ein Overlay referenziert die Base über einen relativen Pfad und überschreibt
gezielt Felder – hier den Image-Tag über `images:`:

```yaml
# overlays/test/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../base

images:
  - name: quay.io/ghilling/quarkus-demo
    newTag: "1.1"

replicas:
  - name: demo
    count: 1
```

`images:` ersetzt Tag (und/oder Registry/Name) **überall**, wo das Image im
gerenderten Ergebnis vorkommt – ohne den Deployment-Text im Overlay
duplizieren zu müssen. `replicas:` ist ein weiterer eingebauter Transformer.

---

## Patches: Feinere Anpassungen

Für alles, was `images:`/`replicas:`/`commonLabels` nicht abdecken, gibt es
**Patches** (Strategic-Merge oder JSON-Patch). Das `prod`-Overlay erhöht
Replicas und setzt zusätzlich Requests/Limits (vgl. Kapitel 8) per Patch:

```yaml
# overlays/prod/kustomization.yaml
resources:
  - ../../base
images:
  - name: quay.io/ghilling/quarkus-demo
    newTag: "1.0"
replicas:
  - name: demo
    count: 3
patches:
  - path: patch-resources.yaml
    target:
      kind: Deployment
      name: demo
```

`patch-resources.yaml` ist ein **Strategic-Merge-Patch**: Es enthält nur den
Ausschnitt der Ressource, der geändert werden soll (hier: `resources:` im
Container), Kustomize merged ihn in die Base-Ressource.

---

## Rendern und Anwenden

```bash
# Nur rendern (zeigt das fertige YAML, ändert nichts im Cluster)
oc kustomize 09-kustomize/overlays/test
oc kustomize 09-kustomize/overlays/prod

# Unterschied zwischen den Overlays anzeigen
diff <(oc kustomize 09-kustomize/overlays/test) <(oc kustomize 09-kustomize/overlays/prod) || true   # diff liefert Exit-Code 1, wenn es Unterschiede gibt

# Anwenden
oc apply -k 09-kustomize/overlays/test
oc apply -k 09-kustomize/overlays/prod
```

`oc apply -k <verzeichnis>` rendert die Kustomization und wendet das Ergebnis
an – äquivalent zu `oc kustomize <verzeichnis> | oc apply -f -`.

---

## Manifeste in diesem Kapitel

- `base/deployment.yaml`, `base/service.yaml`, `base/kustomization.yaml` – gemeinsame Basis
- `overlays/test/kustomization.yaml` – Test-Overlay (Tag `1.1` – die neue Version wird zuerst getestet, 1 Replica)
- `overlays/prod/kustomization.yaml`, `overlays/prod/patch-resources.yaml` – Prod-Overlay (Tag `1.0`, 3 Replicas, Requests/Limits)

---

## Weiterführende Links

- [Kubernetes: Kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/)
- [Kubernetes: Deklaratives Management von Objekten](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/declarative-config/)
- [OpenShift 4.22: CLI Tools](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/cli_tools/index)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
