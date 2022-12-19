#!/bin/sh

cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: hsapp
nodes:
  - role: control-plane
    extraMounts:
    - hostPath: /tmp
      containerPath: /files
    kubeadmConfigPatches:
      - |
        kind: InitConfiguration
        nodeRegistration:
          kubeletExtraArgs:
            node-labels: "ingress-ready=true"
    extraPortMappings:
      - containerPort: 80
        hostPort: 8000
        protocol: TCP
      - containerPort: 443
        hostPort: 8080
        protocol: TCP
EOF