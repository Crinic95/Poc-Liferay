FROM liferay/dxp:2025.q1.0-lts

USER root
ENV LIFERAY_HOME=/opt/liferay

COPY bundles/deploy/ ${LIFERAY_HOME}/deploy/
COPY bundles/osgi/configs/ ${LIFERAY_HOME}/osgi/configs/
COPY bundles/portal-ext.properties ${LIFERAY_HOME}/portal-ext.properties

USER liferay