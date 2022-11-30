
create-image-secret:
	kubectl create secret generic regcred \
    	--from-file=.dockerconfigjson=/Users/alexeyn/.docker/config.json \
    	--type=kubernetes.io/dockerconfigjson \
		-n vvp-jobs

build-local-image:
	nerdctl --namespace=k8s.io build -t local/flink:1.15.2-stream3-no-scala .

start-ammonite:
	amm --predef scripts/flink-amm.sc	