# Anhang: Setup von Visual Studio Code

Dieser Anhang beschreibt die Einrichtung von **Visual Studio Code** (VS Code) für die Arbeit mit diesem Seminar.

---

## Installation

1. **VS Code herunterladen und installieren**: [https://code.visualstudio.com/](https://code.visualstudio.com/)

2. **Dieses Repository klonen**:

```bash
git clone https://github.com/guhilling/containers-kubernetes-intro.git
cd containers-kubernetes-intro
code .
```

---

## Empfohlene Extensions

Folgende Extensions werden für die Arbeit mit diesem Seminar empfohlen:

### Java / Quarkus

| Extension | ID | Beschreibung |
|---|---|---|
| Extension Pack for Java | `vscjava.vscode-java-pack` | JDT LS, Debugger, Maven, Test-Runner |
| Quarkus | `redhat.vscode-quarkus` | Projekt-Generator, Dev Mode, Konfigurations-Unterstützung |

Installation per Kommandozeile:

```bash
code --install-extension vscjava.vscode-java-pack
code --install-extension redhat.vscode-quarkus
```

### Kubernetes / OpenShift

| Extension | ID | Beschreibung |
|---|---|---|
| Kubernetes | `ms-kubernetes-tools.vscode-kubernetes-tools` | YAML-Validierung, Cluster-Explorer, kubectl-Integration |
| YAML | `redhat.vscode-yaml` | YAML-Schema-Validierung (Kubernetes CRDs) |

```bash
code --install-extension ms-kubernetes-tools.vscode-kubernetes-tools
code --install-extension redhat.vscode-yaml
```

### Container

| Extension | ID | Beschreibung |
|---|---|---|
| Docker | `ms-azuretools.vscode-docker` | Containerfile-Highlighting, Image- und Container-Verwaltung |

```bash
code --install-extension ms-azuretools.vscode-docker
```

---

## JDK 21 (Eclipse Temurin) konfigurieren

VS Code benötigt eine lokale JDK-Installation für die Java-Entwicklung.

### JDK installieren (Adoptium Temurin 21)

- **Linux**: `sdk install java 21-tem` (via [SDKMAN](https://sdkman.io/))
- **macOS**: `brew install --cask temurin@21`
- **Windows**: Installer von [https://adoptium.net/](https://adoptium.net/temurin/releases/?version=21)

### VS Code auf JDK 21 zeigen

In `.vscode/settings.json` im Repository-Root:

```json
{
  "java.jdt.ls.java.home": "/path/to/jdk-21",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/path/to/jdk-21",
      "default": true
    }
  ]
}
```

> **Tipp**: Den Pfad zum JDK findet man mit `java -XshowSettings:all -version 2>&1 | grep java.home`.

---

## kubectl / oc konfigurieren

Damit die Kubernetes-Extension funktioniert, muss `kubectl` oder `oc` im PATH sein und eine gültige `kubeconfig` vorhanden sein.

```bash
# Aktuelle Kubeconfig prüfen
kubectl config view
oc config view

# Für OpenShift: Login
oc login https://<cluster-api>:6443 -u <user>
```

VS Code zeigt dann im **Kubernetes**-Explorer (linke Sidebar) alle verfügbaren Cluster, Namespaces, Pods usw.

---

## Red Hat OpenShift Sandbox

Über https://sandbox.redhat.com erhält man einfach Zugriff auf ein eigenes Project in einem aktuellen OpenShift-Cluster. Dort kann man u.a. nutzen:

* (natürlich) OpenShift
* Mehrere Storage Classes/PersistentVolumeClaims (Quotas beachten)
* Netzwerkzugriff über Routen
* Direkten API-Zugriff vom eigenen Rechner
* (OpenShift Virtualization - nicht Teil dieses Kurses)

---

## Workspace-Einstellungen

Empfohlene `.vscode/settings.json` für dieses Repository:

```json
{
  "editor.formatOnSave": true,
  "editor.tabSize": 2,
  "files.eol": "\n",
  "yaml.schemas": {
    "kubernetes": "**/*.yaml"
  },
  "java.compile.nullAnalysis.mode": "automatic"
}
```

---

## Nützliche Tastenkürzel

| Aktion | Shortcut (Linux/Windows) | Shortcut (macOS) |
|---|---|---|
| Command Palette öffnen | `Ctrl+Shift+P` | `Cmd+Shift+P` |
| Terminal öffnen | `Ctrl+` ` ` | `Cmd+` ` ` |
| Datei suchen | `Ctrl+P` | `Cmd+P` |
| Alle Dateien durchsuchen | `Ctrl+Shift+F` | `Cmd+Shift+F` |
| Klasse/Symbol suchen | `Ctrl+T` | `Cmd+T` |
| Refactoring-Menü | `Ctrl+Shift+R` | `Cmd+Shift+R` |

---

## Weiterführende Dokumentation

Siehe [DOCUMENTATION.md](./DOCUMENTATION.md) für externe Referenzen zur
Kubernetes-Dokumentation und zur OpenShift-4.22-Dokumentation.

