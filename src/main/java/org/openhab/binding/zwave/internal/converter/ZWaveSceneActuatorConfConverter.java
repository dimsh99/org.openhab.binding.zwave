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
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSceneActuatorConfCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Jackson
 */
public class ZWaveSceneActuatorConfConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveSceneActuatorConfConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveSceneActuatorConfConverter} class.
     *
     */
    public ZWaveSceneActuatorConfConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveDoorLockCommandClass commandClass = (ZWaveDoorLockCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SCENE_ACTUATOR_CONF, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(commandClass.getValueMessage(),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(serialMessage);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        logger.debug("NODE {}: Handle door lock event {}", event.getNodeId(), event.getType());

        switch ((ZWaveDoorLockCommandClass.Type) event.getType()) {
            case SCENE_ID:
                return handleEventSceneId(channel, event);
            case SCENE_LEVEL:
                return handleEventSceneLevel(channel, event);
            default:
                return null;
        }
    }

    private State handleEventSceneId(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
	return new DecimalType((Integer) event.getValue());
    }

    private State handleEventCondition(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        int value = (int) event.getValue();

        // If we read greater than 99%, then change it to 100%
        // This just appears better in OH otherwise you can't get 100%!
        if (value >= 99) {
            value = 100;
        }


	State state = null;
        switch (channel.getDataType()) {
            case PercentType:
                if (value < 0 || value > 100) {
                    break;
                }

                if (configInvertPercent) {
                    state = new PercentType(100 - value);
                } else {
                    state = new PercentType(value);
                }

                break;
            case OnOffType:
                if (value == 0) {
                    state = OnOffType.OFF;
                } else {
                    state = OnOffType.ON;
                }

                if (configInvertControl) {
                    if (state == OnOffType.ON) {
                        state = OnOffType.OFF;
                    } else {
                        state = OnOffType.ON;
                    }
                }
                break;
            case IncreaseDecreaseType:
                // state = IncreaseDecreaseType.INCREASE;
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;

    }

}
