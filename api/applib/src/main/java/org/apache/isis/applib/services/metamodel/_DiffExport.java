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
package org.apache.isis.applib.services.metamodel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.collections._Streams;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.Facet;
import org.apache.isis.schema.metamodel.v2.FacetAttr;
import org.apache.isis.schema.metamodel.v2.FacetHolder.Facets;
import org.apache.isis.schema.metamodel.v2.Member;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _DiffExport {
    
    StringBuilder toDiff(
            MetamodelDto leftMetamodelDto,
            MetamodelDto rightMetamodelDto) {

        val leftTypesById = new TreeMap<String, DomainClassDto>();
        val rightTypesById = new TreeMap<String, DomainClassDto>();
        
        val facetsById = new TreeMap<String, Facet>();
        final Consumer<? super Facet> facetCollector = facet->facetsById.put(facet.getId(), facet);
        
        visitAllFacets(leftMetamodelDto, facetCollector);
        visitAllFacets(rightMetamodelDto, facetCollector);
        
        val sb = new StringBuilder();
        
        // -- type intersection
        
        val leftTypes = streamTypes(leftMetamodelDto)
                .peek(type->leftTypesById.put(type.getId(), type))
                .map(DomainClassDto::getId)
                .collect(Collectors.toSet());
        
        val rightTypes = streamTypes(rightMetamodelDto)
                .peek(type->rightTypesById.put(type.getId(), type))
                .map(DomainClassDto::getId)
                .collect(Collectors.toSet());
        
        val leftNotInRight = _Sets.minus(leftTypes, rightTypes);
        val rightNotInLeft = _Sets.minus(rightTypes, leftTypes);
        
        leftNotInRight
            .stream()
            .forEach(typeId->sb.append(LEFT_SYMBOL).append(" ").append(typeId).append("\n"));
        rightNotInLeft
            .stream()
            .forEach(typeId->sb.append(RIGHT_SYMBOL).append(" ").append(typeId).append("\n"));

        val inLeftAndRight = _Sets.intersect(leftTypes, rightTypes);
                
        val leftTypeIntersection = inLeftAndRight
                .stream()
                .sorted()
                .map(leftTypesById::get)
                .collect(Can.toCan());
        
        val rightTypeIntersection = inLeftAndRight
                .stream()
                .sorted()
                .map(rightTypesById::get)
                .collect(Can.toCan());
        
        // -- member intersection
        
        val leftMembersByKey = new TreeMap<String, Member>();
        val rightMembersByKey = new TreeMap<String, Member>();
        
        leftTypeIntersection
        .stream()
        .forEach(type->streamMembers(type)
                .forEach(m->{
                    val key = memberKey(type, m);
                    leftMembersByKey.put(key, m);
                    m.setId(key); // rewrite id with key (that includes type id)
                }));
        
        rightTypeIntersection
        .stream()
        .forEach(type->streamMembers(type)
                .forEach(m->{
                    val key = memberKey(type, m);
                    rightMembersByKey.put(key, m);
                    m.setId(key); // rewrite id with key (that includes type id)
                }));
        
        val leftNotInRightM = _Sets.minus(leftMembersByKey.keySet(), rightMembersByKey.keySet());
        val rightNotInLeftM = _Sets.minus(rightMembersByKey.keySet(), leftMembersByKey.keySet());
        
        leftNotInRightM
            .stream()
            .forEach(memberKey->sb.append(LEFT_SYMBOL).append(" ").append(memberKey).append("\n"));
        rightNotInLeftM
            .stream()
            .forEach(memberKey->sb.append(RIGHT_SYMBOL).append(" ").append(memberKey).append("\n"));
        
        val inLeftAndRightM = _Sets.intersect(leftMembersByKey.keySet(), rightMembersByKey.keySet());
        
        val leftMemberIntersection = inLeftAndRightM
                .stream()
                .sorted()
                .map(leftMembersByKey::get)
                .collect(Can.toCan());
        
        val rightMemberIntersection = inLeftAndRightM
                .stream()
                .sorted()
                .map(rightMembersByKey::get)
                .collect(Can.toCan());
        
        // -- report facet differences
        
        facetsById
        .values()
        .forEach(facet->{
            val diffModel = new DiffModel(f->f.getId().equals(facet.getId()), 
                    leftTypeIntersection, rightTypeIntersection,
                    leftMemberIntersection, rightMemberIntersection);
            diff(diffModel, leftMetamodelDto, rightMetamodelDto);

            if(diffModel.isEmpty()) {
                return; // skip (suppress output)
            }
            
            sb.append('[').append(facet.getId()).append(']').append("\n\n");
            sb.append(diffModel.sb.toString());
            sb.append("\n");
            
        });
        
        return sb;
    }
    
    // -- HELPER
    
    private final static String LEFT_SYMBOL = "L"; 
    private final static String RIGHT_SYMBOL = "R";
    private final static String DIFF_SYMBOL = "D";
    
    @RequiredArgsConstructor
    private static class DiffModel {
        final StringBuilder sb = new StringBuilder();
        final Predicate<Facet> facetFilter;
        final Can<DomainClassDto> leftIntersection;
        final Can<DomainClassDto> rightIntersection;
        final Can<Member> leftMemberIntersection;
        final Can<Member> rightMemberIntersection;
        long diffCout;
        boolean isEmpty() {
            return diffCout==0L;
        }
    }
    
    private void diff(DiffModel diffModel, 
            MetamodelDto leftMetamodelDto,
            MetamodelDto rightMetamodelDto) {
        
        // type level facets
        diffModel.leftIntersection.zip(diffModel.rightIntersection, (leftType, rightType)->{
            diffFacets(diffModel, leftType.getId(), leftType.getFacets(), rightType.getFacets());
        });
        
        // member level facets
        diffModel.leftMemberIntersection.zip(diffModel.rightMemberIntersection, (leftMember, rightMember)->{
            diffFacets(diffModel, leftMember.getId(), leftMember.getFacets(), rightMember.getFacets());
        });
        
    }
    
    private void diffFacets(DiffModel diffModel, String typeOrMemberId, 
            Facets leftFacets, Facets rightFacets) {
        
        val sb = diffModel.sb;
        val leftFacet = findFirstFacet(leftFacets, diffModel.facetFilter);
        val rightFacet = findFirstFacet(rightFacets, diffModel.facetFilter);
        
        if(leftFacet.isPresent()) {
            if(!rightFacet.isPresent()) {
                sb.append(LEFT_SYMBOL).append(" ").append(typeOrMemberId).append("\n");
                diffModel.diffCout++;
            } else {
                diffAttrs(diffModel, typeOrMemberId, leftFacet.get(), rightFacet.get());
            }
        } else {
            if(rightFacet.isPresent()) {
                sb.append(RIGHT_SYMBOL).append(" ").append(typeOrMemberId).append("\n");
                diffModel.diffCout++;
            } else {
                // skip (absent in both)
            }
        }
    }
    
    private void diffAttrs(DiffModel diffModel, String typeOrMemberId, Facet leftFacet, Facet rightFacet) {
        
        val sb = diffModel.sb;
        val leftAttrByName = new TreeMap<String, FacetAttr>();
        val rightAttrByName = new TreeMap<String, FacetAttr>();

        val leftAttrNames = streamFacetAttr(leftFacet)
                .peek(attr->leftAttrByName.put(attr.getName(), attr))
                .map(FacetAttr::getName)
                .collect(Collectors.toSet());
        
        val rightAttrNames = streamFacetAttr(rightFacet)
                .peek(attr->rightAttrByName.put(attr.getName(), attr))
                .map(FacetAttr::getName)
                .collect(Collectors.toSet());
        
        val leftNotInRight = _Sets.minus(leftAttrNames, rightAttrNames);
        val rightNotInLeft = _Sets.minus(rightAttrNames, leftAttrNames);
        
        leftNotInRight
            .stream()
            .peek(__->diffModel.diffCout++)
            .forEach(attrName->sb.append(LEFT_SYMBOL)
                    .append(" ").append(typeOrMemberId).append(" ").append(attrName)
                    .append(" ").append(leftAttrByName.get(attrName).getValue())
                    .append("\n"));
        rightNotInLeft
            .stream()
            .peek(__->diffModel.diffCout++)
            .forEach(attrName->sb.append(RIGHT_SYMBOL)
                    .append(" ").append(typeOrMemberId).append(" ").append(attrName)
                    .append(" ").append(rightAttrByName.get(attrName).getValue())
                    .append("\n"));

        val inLeftAndRight = _Sets.intersect(leftAttrNames, rightAttrNames);
                
        val leftAttrIntersection = inLeftAndRight
                .stream()
                .sorted()
                .map(leftAttrByName::get)
                .collect(Can.toCan());
        
        val rightAttrIntersection = inLeftAndRight
                .stream()
                .sorted()
                .map(rightAttrByName::get)
                .collect(Can.toCan());
        
     
        leftAttrIntersection.zip(rightAttrIntersection, (leftAttr, rightAttr)->{
            _Assert.assertEquals(leftAttr.getName(), rightAttr.getName());
            if(Objects.equals(leftAttr.getValue(), rightAttr.getValue())) {
                return; // skip (same attr values)
            }
            
            sb.append(DIFF_SYMBOL)
            .append(" ").append(typeOrMemberId).append(" ").append(leftAttr.getName())
            .append(" ").append(leftAttr.getValue()).append(" <-> ").append(rightAttr.getValue())
            .append("\n");
            
            diffModel.diffCout++;
        });
        
        
    }

    private Stream<DomainClassDto> streamTypes(MetamodelDto mmDto) {
        return mmDto.getDomainClassDto()
                .stream()
                .sorted((a, b)->a.getId().compareTo(b.getId()));
    }
    
    private Stream<Member> streamMembers(DomainClassDto typeDto) {

        return _Streams.concat(
        
            Optional.ofNullable(typeDto.getProperties())
            .map(props->props.getProp())
            .map(List::stream)
            .orElse(Stream.empty())
            .sorted((a, b)->a.getId().compareTo(b.getId())),
            
            Optional.ofNullable(typeDto.getCollections())
            .map(colls->colls.getColl())
            .map(List::stream)
            .orElse(Stream.empty())
            .sorted((a, b)->a.getId().compareTo(b.getId())),

            Optional.ofNullable(typeDto.getActions())
            .map(acts->acts.getAct())
            .map(List::stream)
            .orElse(Stream.empty())
            .sorted((a, b)->a.getId().compareTo(b.getId()))
        );

    }
    
    private Optional<Facet> findFirstFacet(Facets x, Predicate<Facet> filter) {
        return Optional.ofNullable(x)
                .map(Facets::getFacet)
                .map(List::stream)
                .orElse(Stream.empty())
                .filter(filter)
                .findFirst();
    }
    
    private Stream<Facet> streamFacets(DomainClassDto x) {
        return Optional.ofNullable(x.getFacets())
                .map(Facets::getFacet)
                .map(List::stream)
                .orElse(Stream.empty());
    }
    
    private Stream<Facet> streamFacets(Member x) {
        return Optional.ofNullable(x.getFacets())
                .map(Facets::getFacet)
                .map(List::stream)
                .orElse(Stream.empty());
    }
    
    private Stream<FacetAttr> streamFacetAttr(Facet x) {
        return Optional.ofNullable(x.getAttr())
                .map(List::stream)
                .orElse(Stream.empty())
                .sorted((a, b)->a.getName().compareTo(b.getName()));
    }

    private String memberKey(DomainClassDto type, Member member) {
        return type.getId() + "#" + member.getId();
    }
    
    private void visitAllFacets(MetamodelDto mmDto, final Consumer<? super Facet> onFacet) {
        streamTypes(mmDto)
        .peek(x->streamFacets(x).forEach(onFacet))
        .flatMap(_DiffExport::streamMembers)
        .flatMap(_DiffExport::streamFacets)
        .forEach(onFacet);
    }
    
}
