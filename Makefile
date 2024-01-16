
create-image-secret:
	kubectl create secret generic regcred \
    	--from-file=.dockerconfigjson=/Users/alexeyn/.docker/config.json \
    	--type=kubernetes.io/dockerconfigjson \
		-n vvp-jobs

build-local-image:
	nerdctl --namespace=k8s.io build -t local/flink:1.15.2-stream3-no-scala -f Dockerfile-Ververica .

build-scala-image:
	docker build -t flink:1.17.2-stream1-no-scala -f Dockerfile-Ververica .

build-apache-scala-image:
	docker build -t flink:1.15.2-my-job-scala3 .

build-gateway:
	docker build -t eu.gcr.io/da-fe-212612/vvp/vvp-gateway:2.12.0-iceberg-15 -f Dockerfile-Connector .

launch-app:
	flink run-application -p 3 -t kubernetes-application \
		-c org.example.fraud.FraudDetectionJob \
		-Dtaskmanager.numberOfTaskSlots=2 \
		-Dkubernetes.rest-service.exposed.type=NodePort \
		-Dkubernetes.cluster-id=fraud-detection \
		-Dkubernetes.container.image=flink:1.15.2-my-job-scala3 \
		-Dkubernetes.service-account=flink-service-account \
		local:///opt/flink/usrlib/my-flink-job.jar

start-ammonite:
	amm --predef scripts/flink-amm.sc	