#!/bin/bash
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR=$( dirname "$0" )
cd $SCRIPT_DIR || exit 1

echo "==================="
echo "= MIGRATION NOTES ="
echo "==================="
echo "the java file list (below) was migrated to use the global document index instead (module system overview)"
echo "hence this script is a no-op"
exit 0

SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

for dir in services
do
  rm -rf examples/$dir
  mkdir -p examples/$dir
  #cp -R $SRC_APPLIB/$dir/* examples/$dir
done

export javaFiles="
acceptheader/AcceptHeaderService.java
appfeat/ApplicationFeatureRepository.java
appfeat/ApplicationMemberType.java
audit/AuditerService.java
bookmark/Bookmark.java
bookmark/BookmarkHolder.java
bookmark/BookmarkService.java
bookmarkui/BookmarkUiService.java
clock/ClockService.java
commanddto/processor/CommandDtoProcessor.java
commanddto/processor/spi/CommandDtoProcessorService.java
confview/ConfigurationMenu.java
confview/ConfigurationProperty.java
confview/ConfigurationViewService.java
conmap/ContentMappingService.java
error/ErrorDetails.java
error/ErrorReportingService.java
error/Ticket.java
eventbus/EventBusService.java
exceprecog/ExceptionRecognizer.java
exceprecog/ExceptionRecognizerService.java
factory/FactoryService.java
grid/GridLoaderService.java
grid/GridService.java
grid/GridSystemService.java
health/HealthCheckService.java
hint/HintStore.java
homepage/HomePageResolverService.java
i18n/LocaleProvider.java
i18n/TranslationService.java
i18n/TranslationsResolver.java
iactn/Interaction.java
iactn/InteractionContext.java
inject/ServiceInjector.java
jaxb/JaxbService.java
layout/LayoutService.java
layout/LayoutServiceMenu.java
linking/DeepLinkService.java
menu/MenuBarsLoaderService.java
menu/MenuBarsService.java
message/MessageService.java
metamodel/BeanSort.java
metamodel/DomainMember.java
metamodel/DomainModel.java
metamodel/MetaModelService.java
metamodel/MetaModelServiceMenu.java
metrics/MetricsService.java
publish/PublishedObjects.java
publish/PublisherService.java
queryresultscache/QueryResultCacheControl.java
queryresultscache/QueryResultsCache.java
repository/EntityState.java
repository/RepositoryService.java
routing/RoutingService.java
scratchpad/Scratchpad.java
session/SessionLoggingService.java
sudo/SudoService.java
swagger/SwaggerService.java
tablecol/TableColumnOrderService.java
title/TitleService.java
urlencoding/UrlEncodingService.java
user/RoleMemento.java
user/UserMemento.java
user/UserService.java
userprof/UserProfileService.java
userreg/EmailNotificationService.java
userreg/UserRegistrationService.java
wrapper/WrapperFactory.java
wrapper/control/AsyncControl.java
wrapper/control/ControlAbstract.java
wrapper/control/ExceptionHandler.java
wrapper/control/SyncControl.java
xactn/TransactionId.java
xactn/TransactionService.java
xactn/TransactionState.java
xml/XmlService.java
xmlsnapshot/XmlSnapshotService.java
"

for javaFile in $javaFiles
do
  mkdir -p "$(dirname "examples/services/$javaFile")"
  cp "$SRC_APPLIB/services/$javaFile" "examples/services/$javaFile"
done


