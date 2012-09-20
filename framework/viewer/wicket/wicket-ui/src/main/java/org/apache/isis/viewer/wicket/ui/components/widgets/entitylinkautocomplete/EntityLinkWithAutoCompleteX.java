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

package org.apache.isis.viewer.wicket.ui.components.widgets.entitylinkautocomplete;

import org.apache.isis.viewer.wicket.model.models.EntityModel;
//import com.vaynberg.wicket.select2.ChoiceProvider;
//import com.vaynberg.wicket.select2.Select2Choice;
//import com.vaynberg.wicket.select2.TextChoiceProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitylink.EntityLink;

public class EntityLinkWithAutoCompleteX extends EntityLink {

    private static final long serialVersionUID = 1L;
    private static final String ID_AUTO_COMPLETE = "autoComplete";

//    private Select2Choice<ObjectAdapter> autoCompleteField;

    public EntityLinkWithAutoCompleteX(String id, EntityModel entityModel) {
        super(id, entityModel);
    }


    
//    protected void doSyncWithInputWhenChoices() {
//        permanentlyHide(ID_AUTO_COMPLETE);
//    }
//
//    @Override
//    protected void doSyncWithInputOther() {
//        if(hasAutoComplete()){
//
//            // serializable, referenced by the AutoCompletionChoicesProvider below  
//            final EntityModel entityModel = getEntityModel();
//            
//            ChoiceProvider<ObjectAdapter> provider = new TextChoiceProvider<ObjectAdapter>() {
//
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                protected String getDisplayText(ObjectAdapter choice) {
//                    return choice.titleString();
//                }
//
//                @Override
//                protected Object getId(ObjectAdapter choice) {
//                    final RootOid oid = (RootOid) choice.getOid();
//                    return oid.asBookmark().toString();
//                }
//
//                @Override
//                public void query(String term, int page, com.vaynberg.wicket.select2.Response<ObjectAdapter> response) {
//                    final ObjectSpecification typeOfSpecification = entityModel.getTypeOfSpecification();
//                    final AutoCompleteFacet autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
//                    final List<ObjectAdapter> results = autoCompleteFacet.execute(term);
//                    response.addAll(results);
//                }
//
//                @Override
//                public Collection<ObjectAdapter> toChoices(Collection<String> ids) {
//                    Function<String, ObjectAdapter> function = new Function<String, ObjectAdapter>() {
//
//                        @Override
//                        public ObjectAdapter apply(String input) {
//                            final Bookmark bookmark = new Bookmark(input);
//                            final RootOid oid = RootOidDefault.create(bookmark);
//
//                            final ObjectSpecification typeOfSpecification = entityModel.getTypeOfSpecification();
//                            final AutoCompleteFacet autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
//
//                            // REVIEW: this is hacky, but need a serializable way of getting the AdapterManager...
//                            return autoCompleteFacet.lookup(oid);
//                        }
//                    };
//                    return Collections2.transform(ids, function);
//                }
//
//            };
//            final ModelAbstract<ObjectAdapter> model = new ModelAbstract<ObjectAdapter>(){
//
//                private static final long serialVersionUID = 1L;
//                
//                @Override
//                protected ObjectAdapter load() {
//                    return getPendingElseCurrentAdapter();
//                }
//                
//            };
//            autoCompleteField = new Select2Choice<ObjectAdapter>(ID_AUTO_COMPLETE, model, provider);
//            addOrReplace(autoCompleteField);
//            
//            // no need for link, since can see in drop-down
//            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
//
//            // no need for the 'null' title, since if there is no object yet
//            // can represent this fact in the drop-down
//            permanentlyHide(ID_ENTITY_TITLE_NULL);
//            
//            // hide links
//            permanentlyHide(ID_FIND_USING, ID_ENTITY_CLEAR_LINK, ID_ENTITY_DETAILS_LINK_LABEL);
//
//        } else {
//            permanentlyHide(ID_AUTO_COMPLETE);
//        }
//
//    }
//
//    @Override
//    protected void doSyncVisibilityAndUsability(boolean mutability) {
//        if(autoCompleteField != null) {
//            autoCompleteField.setEnabled(mutability);
//        }
//
//        if(hasAutoComplete()) {
//            permanentlyHide(ID_ENTITY_ICON_AND_TITLE, ID_ENTITY_DETAILS_LINK);
//        }
//    }
//    
//    @Override
//    protected void doConvertInput() {
//        if(getEntityModel().isEditMode() && hasAutoComplete()) {
//            // flush changes to pending
//            onSelected(autoCompleteField.getConvertedInput());
//        }
//    }
//    
//    private boolean hasAutoComplete() {
//        final ObjectSpecification typeOfSpecification = getEntityModel().getTypeOfSpecification();
//        final AutoCompleteFacet autoCompleteFacet = 
//                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
//        return autoCompleteFacet != null;
//    }


    
    
}
