
create-image-secret:
	kubectl create secret generic regcred \
    	--from-file=.dockerconfigjson=/Users/alexeyn/.docker/config.json \
    	--type=kubernetes.io/dockerconfigjson \
		-n vvp-jobs

build-local-image:
	nerdctl --namespace=k8s.io build -t local/flink:1.15.2-stream3-no-scala .

build-scala-image:
	docker build -t flink:1.15.2-stream3-scala3-java11 .

start-ammonite:
	amm --predef scripts/flink-amm.sc	