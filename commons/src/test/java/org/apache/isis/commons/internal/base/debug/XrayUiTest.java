/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.internal.base.debug;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayDataModel;
import org.apache.isis.commons.internal.debug.xray.XrayModel;
import org.apache.isis.commons.internal.debug.xray.XrayModel.Stickiness;
import org.apache.isis.commons.internal.debug.xray.XrayUi;

import lombok.val;

class XrayUiTest {

    public static void main(final String[] args) throws InterruptedException {
        XrayUi.start(JFrame.EXIT_ON_CLOSE);

        val ex = Executors.newSingleThreadExecutor();
        ex.execute(new SampleLogs());
        ex.shutdown();
        ex.awaitTermination(1L, TimeUnit.SECONDS);

        XrayUi.updateModel(XrayUiTest::populate);
    }

    private static class SampleLogs implements Runnable {

        @Override
        public void run() {
            Can.of("Hallo World! from Can")
            .forEach(this::log);

            List.of("Hallo World! from List")
            .forEach(this::log);
        }

        @SuppressWarnings("deprecation")
        private void log(final String x) {
            _Debug.log(x);
        }

    }

    private static void populate(final XrayModel model) {

        val root = model.getRootNode();

        val keyValueData = model.addDataNode(
                root, new XrayDataModel.KeyValue("id1", "KeyValue", Stickiness.CANNOT_DELETE_NODE));
        keyValueData.getData().put("hi", "there");
        keyValueData.getData().put("how", "you");

        val sequenceData = model.addDataNode(
                root, new XrayDataModel.Sequence("id2", "Sequence", Stickiness.CANNOT_DELETE_NODE))
                .getData();

        sequenceData.alias("thread", "Thread-0");
        sequenceData.alias("test", "JUnit Test");
        sequenceData.alias("ix", "Interaction\nxxx-yyy-zzz");
        sequenceData.alias("tx", "Transaction");
        sequenceData.alias("ex", "Execution\n- act\n- prop\n- coll");

        sequenceData.enter("thread", "test");
        sequenceData.enter("test", "ix", "run anonymous");
        sequenceData.activate("ix");
        sequenceData.enter("ix", "tx", "require NEW");
        sequenceData.enter("ix", "ex", "execute");

        sequenceData.exit("ex", "ix");
        sequenceData.exit("tx", "ix", "exit\n(after commit/rollback/unknown)");
        sequenceData.exit("ix", "test", "exit");
        sequenceData.deactivate("ix");
        sequenceData.exit("test", "thread", "exit");

        model.addContainerNode(root, "Container", Stickiness.CAN_DELETE_NODE);

    }



}
