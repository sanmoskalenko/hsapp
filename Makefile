up:
	cd devops/docker && docker-compose up

down:
	cd devops/docker && docker-compose down

kube-up:
	cd devops/k8s && ./create-cluster.sh && kubectl apply -f statefulset.yaml,deployment.yaml

kube-down:
	docker stop hsapp-control-plane && docker rm hsapp-control-plane

jar:
	lein uberjar

#build:
#	docker build --pull --rm -f "Dockerfile" -t sanmoskalenko/hsapp:latest "." && docker push sanmoskalenko/hsapp:latest

deps:
	brew install clojure/tools/clojure && brew install leiningen && brew install kubectl && brew install kind && brew install node && lein deps && npm run install

prj-deps:
	lein deps && npm run install