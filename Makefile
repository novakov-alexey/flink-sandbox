
create-image-secret:
	kubectl create secret generic regcred \
    	--from-file=.dockerconfigjson=/Users/alexeyn/.docker/config.json \
    	--type=kubernetes.io/dockerconfigjson \
		-n vvp-jobs

build-local-image:
	nerdctl --namespace=k8s.io build -t local/flink:1.19.0-stream2-scala_2.12-java11 -f Dockerfile-Ververica .

build-scala-image:
	cd docker && docker build -t flink:1.19.0-stream2-scala_2.12-java11 -f Dockerfile-Ververica .
tag-scala-image:
	cd docker && docker tag flink:1.19.0-stream2-scala_2.12-java11 eu.gcr.io/da-fe-212612/flink:1.19.0-stream2-scala_2.12-java11

build-apache-scala-image:
	docker build -t flink:1.15.2-my-job-scala3 .

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

build-remote-jar:
	scala-cli --power package scripts/WordCountApp.scala -o WordCountApp.jar --force

run-remote-jar:
	scala-cli run scripts/WordCountApp.scala --suppress-outdated-dependency-warning