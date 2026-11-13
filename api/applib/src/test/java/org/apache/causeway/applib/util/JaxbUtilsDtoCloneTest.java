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
package org.apache.causeway.applib.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.util.schema.MemberExecutionDtoUtils;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

class JaxbUtilsDtoCloneTest {

    @Test
    void dtoCloning() {

        // ActionInvocationDto hex dump

        var dtoHexDump = "3c 3f 78 6d 6c 20 76 65 72 73 69 6f 6e 3d 22 31 2e 30 22 20 65 6e 63 6f 64 69 6e "
                + "67 3d 22 55 54 46 2d 38 22 20 73 74 61 6e 64 61 6c 6f 6e 65 3d 22 79 65 73 22 3f 3e 3c 41 63 74 "
                + "69 6f 6e 49 6e 76 6f 63 61 74 69 6f 6e 44 74 6f 20 78 6d 6c 6e 73 3a 69 78 6e 3d 22 68 74 74 70 "
                + "3a 2f 2f 63 61 75 73 65 77 61 79 2e 61 70 61 63 68 65 2e 6f 72 67 2f 73 63 68 65 6d 61 2f 69 78 "
                + "6e 22 20 78 6d 6c 6e 73 3a 63 6d 64 3d 22 68 74 74 70 3a 2f 2f 63 61 75 73 65 77 61 79 2e 61 70 "
                + "61 63 68 65 2e 6f 72 67 2f 73 63 68 65 6d 61 2f 63 6d 64 22 20 78 6d 6c 6e 73 3a 63 6f 6d 3d 22 "
                + "68 74 74 70 3a 2f 2f 63 61 75 73 65 77 61 79 2e 61 70 61 63 68 65 2e 6f 72 67 2f 73 63 68 65 6d "
                + "61 2f 63 6f 6d 6d 6f 6e 22 3e 3c 69 78 6e 3a 73 65 71 75 65 6e 63 65 3e 30 3c 2f 69 78 6e 3a 73 "
                + "65 71 75 65 6e 63 65 3e 3c 69 78 6e 3a 74 61 72 67 65 74 20 74 79 70 65 3d 22 73 69 6d 70 6c 65 "
                + "2e 53 69 6d 70 6c 65 4f 62 6a 65 63 74 73 22 20 69 64 3d 22 31 22 2f 3e 3c 69 78 6e 3a 6c 6f 67 "
                + "69 63 61 6c 4d 65 6d 62 65 72 49 64 65 6e 74 69 66 69 65 72 3e 73 69 6d 70 6c 65 2e 53 69 6d 70 "
                + "6c 65 4f 62 6a 65 63 74 73 23 63 72 65 61 74 65 3c 2f 69 78 6e 3a 6c 6f 67 69 63 61 6c 4d 65 6d "
                + "62 65 72 49 64 65 6e 74 69 66 69 65 72 3e 3c 69 78 6e 3a 75 73 65 72 6e 61 6d 65 3e 73 76 65 6e "
                + "3c 2f 69 78 6e 3a 75 73 65 72 6e 61 6d 65 3e 3c 69 78 6e 3a 6d 65 74 72 69 63 73 3e 3c 69 78 6e "
                + "3a 74 69 6d 69 6e 67 73 3e 3c 63 6f 6d 3a 73 74 61 72 74 65 64 41 74 3e 32 30 32 33 2d 30 31 2d "
                + "31 33 54 31 38 3a 34 35 3a 34 32 2e 32 39 33 2b 30 31 3a 30 30 3c 2f 63 6f 6d 3a 73 74 61 72 74 "
                + "65 64 41 74 3e 3c 63 6f 6d 3a 63 6f 6d 70 6c 65 74 65 64 41 74 3e 32 30 32 33 2d 30 31 2d 31 33 "
                + "54 31 38 3a 34 35 3a 34 32 2e 32 39 34 2b 30 31 3a 30 30 3c 2f 63 6f 6d 3a 63 6f 6d 70 6c 65 74 "
                + "65 64 41 74 3e 3c 2f 69 78 6e 3a 74 69 6d 69 6e 67 73 3e 3c 69 78 6e 3a 6f 62 6a 65 63 74 43 6f "
                + "75 6e 74 73 3e 3c 69 78 6e 3a 6c 6f 61 64 65 64 20 62 65 66 6f 72 65 3d 22 32 34 22 20 61 66 74 "
                + "65 72 3d 22 32 34 22 2f 3e 3c 69 78 6e 3a 64 69 72 74 69 65 64 20 62 65 66 6f 72 65 3d 22 32 34 "
                + "22 20 61 66 74 65 72 3d 22 30 22 2f 3e 3c 2f 69 78 6e 3a 6f 62 6a 65 63 74 43 6f 75 6e 74 73 3e "
                + "3c 2f 69 78 6e 3a 6d 65 74 72 69 63 73 3e 3c 69 78 6e 3a 70 61 72 61 6d 65 74 65 72 73 3e 3c 63 "
                + "6d 64 3a 70 61 72 61 6d 65 74 65 72 20 6e 61 6d 65 3d 22 4e 61 6d 65 22 20 74 79 70 65 3d 22 73 "
                + "74 72 69 6e 67 22 3e 3c 63 6f 6d 3a 73 74 72 69 6e 67 3e 64 64 3c 2f 63 6f 6d 3a 73 74 72 69 6e "
                + "67 3e 3c 2f 63 6d 64 3a 70 61 72 61 6d 65 74 65 72 3e 3c 2f 69 78 6e 3a 70 61 72 61 6d 65 74 65 "
                + "72 73 3e 3c 69 78 6e 3a 72 65 74 75 72 6e 65 64 20 74 79 70 65 3d 22 72 65 66 65 72 65 6e 63 65 "
                + "22 3e 3c 63 6f 6d 3a 72 65 66 65 72 65 6e 63 65 20 74 79 70 65 3d 22 73 69 6d 70 6c 65 2e 53 69 "
                + "6d 70 6c 65 4f 62 6a 65 63 74 22 20 69 64 3d 22 32 38 39 22 2f 3e 3c 2f 69 78 6e 3a 72 65 74 75 "
                + "72 6e 65 64 3e 3c 2f 41 63 74 69 6f 6e 49 6e 76 6f 63 61 74 69 6f 6e 44 74 6f 3e";

        var dtoAsBytes = _Bytes.ofHexDump(dtoHexDump);

        // verify that we can reproduce a byte array from its stringified representation
        assertEquals(dtoHexDump, _Bytes.hexDump(dtoAsBytes));

        var mapper = MemberExecutionDtoUtils.dtoMapper(ActionInvocationDto.class);
        var dto = mapper.read(DataSource.ofBytes(dtoAsBytes));

        assertNotNull(dto);
        assertEquals(ActionInvocationDto.class, dto.getClass());
    }

}
