FROM liferay/jar-runner:latest

COPY --chown=liferay:liferay build/libs/7-registration-create-user.jar /opt/liferay/jar-runner.jar

ENV LIFERAY_JAR_RUNNER_JAVA_OPTS="-Xmx512m"