# Anhang: Weiterführende Dokumentation

Diese Übersicht sammelt die wichtigsten externen Referenzen zu den im
Seminar behandelten Themen: die offizielle Kubernetes-Dokumentation sowie
die Red Hat OpenShift Container Platform Dokumentation in der aktuell
verwendeten Version **4.22**.

Am Ende jedes Kapitel-`README.md` findet sich zusätzlich ein kurzer
Abschnitt "Weiterführende Links" mit den für das jeweilige Kapitel
relevantesten Verweisen aus dieser Liste.

## Kubernetes-Referenzdokumentation

- [Kubernetes Documentation Home](https://kubernetes.io/docs/home/) – Einstiegspunkt in die komplette Doku
- [Konzepte: Übersicht](https://kubernetes.io/docs/concepts/overview/) – Architektur, Objekte, Control Plane
- [Cluster-Architektur](https://kubernetes.io/docs/concepts/architecture/control-plane-node-communication/)
- [kubectl-Referenz](https://kubernetes.io/docs/reference/kubectl/)
- [kubectl apply (Referenz)](https://kubernetes.io/docs/reference/kubectl/generated/kubectl_apply/)
- [Deklaratives Management von Objekten](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/declarative-config/)
- [Server-Side Apply](https://kubernetes.io/docs/reference/using-api/server-side-apply/)
- [Kustomize (Kustomization)](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/)
- [Namespaces](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)
- [Pods](https://kubernetes.io/docs/concepts/workloads/pods/)
- [Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [Jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/)
- [CronJobs](https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/)
- [Assigning Pods to Nodes (Affinity)](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/)
- [Pod Topology Spread Constraints](https://kubernetes.io/docs/concepts/scheduling-eviction/topology-spread-constraints/)
- [Taints and Tolerations](https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/)
- [Container-Images](https://kubernetes.io/docs/concepts/containers/images/)
- [Services](https://kubernetes.io/docs/concepts/services-networking/service/)
- [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)
- [NetworkPolicies](https://kubernetes.io/docs/concepts/services-networking/network-policies/)
- [ConfigMaps](https://kubernetes.io/docs/concepts/configuration/configmap/)
- [Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)
- [Ressourcen verwalten (Requests/Limits)](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)
- [ResourceQuotas](https://kubernetes.io/docs/concepts/policy/resource-quotas/)
- [LimitRange](https://kubernetes.io/docs/concepts/policy/limit-range/)
- [Liveness-, Readiness- und Startup-Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [RBAC (Role-Based Access Control)](https://kubernetes.io/docs/reference/access-authn-authz/rbac/)
- [ServiceAccounts](https://kubernetes.io/docs/concepts/security/service-accounts/)
- [Pod Security Standards](https://kubernetes.io/docs/concepts/security/pod-security-standards/)
- [Persistent Volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
- [StorageClasses](https://kubernetes.io/docs/concepts/storage/storage-classes/)
- [Volume-Snapshots](https://kubernetes.io/docs/concepts/storage/volume-snapshots/)

## OpenShift Container Platform 4.22 Dokumentation

Basis-URL: `https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/`

- [Architecture](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/architecture/index)
- [CLI Tools (oc/kubectl)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/cli_tools/index)
- [API Overview](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/api_overview/index)
- [Building Applications](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/building_applications/index)
- [Images](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/images/index)
- [Networking Overview](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/networking_overview/index)
- [Ingress and Load Balancing (Routes)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/ingress_and_load_balancing/index)
- [Network Security (NetworkPolicies)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/network_security/index)
- [Nodes (Requests/Limits, Probes, Quotas)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/nodes/index)
- [Storage](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/storage/index)
- [Authentication and Authorization (RBAC)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/authentication_and_authorization/index)
- [RBAC APIs](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/rbac_apis/index)
- [Security and Compliance (SCCs)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/security_and_compliance/index)
- [GitOps](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/gitops/index)
- [CI/CD Overview](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/cicd_overview/index)

> Hinweis: `docs.openshift.com` leitet auf `docs.redhat.com` weiter. Für
> andere Versionen `4.22` im Pfad einfach durch die gewünschte Version
> ersetzen (z. B. `4.21`).

## Red Hat OpenShift Sandbox

- [Red Hat Developer Sandbox](https://developers.redhat.com/developer-sandbox) – kostenloser, zeitlich begrenzter OpenShift-Zugang ohne Admin-Rechte, wie im Seminar verwendet
