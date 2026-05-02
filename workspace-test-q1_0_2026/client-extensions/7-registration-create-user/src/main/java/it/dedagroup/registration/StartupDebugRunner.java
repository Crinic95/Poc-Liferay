package it.dedagroup.registration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupDebugRunner implements CommandLineRunner {

    @Value("${liferay.base-url:}")
    private String liferayBaseUrl;

    @Value("${com.liferay.lxc.dxp.server.protocol:}")
    private String dxpProtocol;

    @Value("${com.liferay.lxc.dxp.mainDomain:}")
    private String dxpMainDomain;

    @Value("${com.liferay.lxc.dxp.domains:}")
    private String dxpDomains;

    @Override
    public void run(String... args) {
        System.out.println("liferay.base-url = " + liferayBaseUrl);
        System.out.println("com.liferay.lxc.dxp.server.protocol = " + dxpProtocol);
        System.out.println("com.liferay.lxc.dxp.mainDomain = " + dxpMainDomain);
        System.out.println("com.liferay.lxc.dxp.domains = " + dxpDomains);
    }
}