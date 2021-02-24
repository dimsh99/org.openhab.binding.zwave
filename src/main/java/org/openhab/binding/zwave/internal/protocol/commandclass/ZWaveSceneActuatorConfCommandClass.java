/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the scene actuator command class.
 *
 * @author Chris Jackson
 *
 */
@XStreamAlias("COMMAND_CLASS_SCENE_ACTUATOR_CONF")
public class ZWaveSceneActuatorConfCommandClass extends ZWaveCommandClass {
    public enum Type {
        SCENE_ID,
        SCENE_LEVEL
    }

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSceneActuatorConfigurationCommandClass.class);

    private static final int SCENE_ACTUATOR_CONF_SET = 0x01;
    private static final int SCENE_ACTUATOR_CONF_GET = 0x02;
    private static final int SCENE_ACTUATOR_CONF_REPORT = 0x03;
    /**
     * Creates a new instance of the ZWaveSceneActuatorConfigurationCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveSceneActuatorConfigurationCommandClass(ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SCENE_ACTUATOR_CONF;
    }

    /**
     * Process SCENE_ACTUATOR_CONF_REPORT
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = SCENE_ACTUATOR_CONF_REPORT, name = "SCENE_ACTUATOR_CONF_REPORT")
    public void handleProtectionReport(ZWaveCommandClassPayload payload, int endpoint) {
        int sceneId = payload.getPayloadByte(2);
	int sceneLevel = payload.getPayloadByte(3);
        int sceneTime = payload.getPayloadByte(4);

        logger.debug("NODE {}: Scene actuator config report: Scene {}, Level {}, Time {}", getNode().getNodeId(), sceneId, sceneLevel, sceneTime);


        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), sceneId, Type.SCENE_ID);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), sceneLevel, Type.SCENE_LEVEL);
        getController().notifyEventListeners(zEvent);
    }
}
