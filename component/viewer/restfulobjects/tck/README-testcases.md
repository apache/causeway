<!--
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
-->

<pre>

[incorrect]_thenResponseCode_405_bad

Get_whenQueryArg_xRoFollowLinks_ok
Get_whenQueryArg_xRoDomainModel_ok

Get_whenRequestHeaders_Accept_isInvalid_bad
Get_whenRequestHeaders_Accept_ok


[modifying]_givenDisabled_thenResponseCode_203_TODO

[modifying]_givenEtag_whenIfMatchHeaderDoesMatch_ok
[modifying]_givenEtag_whenIfMatchHeaderDoesNotMatch_bad

[modifying]_givenXxx_whenArgsValid_thenXxx_ok_TODO


[modifying]_whenArgsMandatoryButMissing_bad_TODO
rename from
[modifying]_whenNoArg_bad_TODO


[modifying]_then[PostConditionsHappyCase]_TODO

[modifying]_whenArgIsHrefAndLinksToNonExistentEntity_bad_TODO
[modifying]_whenArgIsHrefAndLinksToEntityOfWrongType_bad_TODO

[modifying]_whenArgValueIsValid_andQueryArg_XRoValidateOnly_2xx_TODO
[modifying]_whenArgValueIsInvalid_andQueryArg_XRoValidateOnly_4xx_TODO
[modifying]_whenArgValueIsInvalid_bad_TODO

[modifying]_whenArgIsMalformed_bad_TODO

[anyValid]_whenDoesntExistOid_thenResponseCode_404_TODO
[anyValid]_whenDoesntExistMember_thenResponseCode_404_TODO
[anyValid]_givenHidden_thenResponseCode_404_TODO

[anyValid]_thenRepresentation_ok_TODO
[anyValid]_thenResponseCode_200_ok
[anyValid]_thenResponseHeaders_ContentType_ok
[anyValid]_thenResponseHeaders_ContentLength_ok
[anyValid]_thenResponseHeaders_CacheControl_ok
[anyValid]_thenResponseHeaders_eTag_ok







Delete_whenLinkToEntityThatExistsButIsNotInCollection_ok_TODO
Delete_whenHrefArgLinksToEntityInCollection_ok_TODO
Put_whenArgsValid_thenMultiplePropertyUpdate_TODO

Put_givenEntityAlreadyInCollection_whenArgsValid_thenNoChange_ok_TODO
Put_givenEntityNotInCollection_whenArgsValid_thenEntityAddedFromCollection_ok_TODO

Get_givenHiddenMembers_thenRepresentation_ok_TODO
</pre>
