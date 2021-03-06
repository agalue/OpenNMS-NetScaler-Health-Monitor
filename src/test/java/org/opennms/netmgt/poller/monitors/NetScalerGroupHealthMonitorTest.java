/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

/**
 * Test class for NetScalerGroupHealthMonitorTest.
 *
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 */
public class NetScalerGroupHealthMonitorTest {
    static final int TEST_SNMP_PORT = 9161;
    static final String TEST_IP_ADDRESS = "127.0.0.1";
    private MockSnmpAgent agent;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        SnmpPeerFactory factory = new SnmpPeerFactory("<snmp-config read-community=\"public\" retry=\"1\" timeout=\"1800\" version=\"2c\" port=\"" + TEST_SNMP_PORT + "\" />");
        SnmpPeerFactory.setInstance(factory);
        agent = MockSnmpAgent.createAgentAndRun(new File("src/test/resources/netscaler-health.properties").toURI().toURL(), TEST_IP_ADDRESS + "/" + TEST_SNMP_PORT);
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        agent.stop();
    }

    @Test
    public void testAvailable() throws Exception {
        NetScalerGroupHealthMonitor monitor = new NetScalerGroupHealthMonitor();
        PollStatus status = monitor.poll(createMonitor(), createBasicParams());
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testUnavailable() throws Exception {
        NetScalerGroupHealthMonitor monitor = new NetScalerGroupHealthMonitor();
        Map<String, Object> parameters =  createBasicParams();
        parameters.put("group-health", 70);
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        Assert.assertTrue(status.getReason().contains("there are 2 of 3 servers active"));
    }

    private Map<String, Object> createBasicParams() {
        Map<String, Object> parameters = new HashMap<String,Object>();
        parameters.put("group-name", "p_d_wf-iis_http_s_grp");
        return parameters;
    }

    private MonitoredService createMonitor() throws UnknownHostException {
        MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "NetScaler-TEST");
        return svc;
    }

}
