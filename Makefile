
create-image-secret:
	kubectl create secret generic regcred \
    	--from-file=.dockerconfigjson=/Users/alexeyn/.docker/config.json \
    	--type=kubernetes.io/dockerconfigjson \
		-n vvp-jobs

build-local-image:
	nerdctl --namespace=k8s.io build -t local/flink:1.15.2-stream3-no-scala -f Dockerfile-Ververica .

build-scala-image:
	docker build -t flink:1.18.1-scala3 -f Dockerfile-Ververica .

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