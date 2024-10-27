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
package org.apache.causeway.core.metamodel.object;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.object.ManagedObject.Specialization;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics;

import lombok.SneakyThrows;

class ManagedObjectTest {

    private MetaModelContext_forTesting mmc;
    private SpecificationLoader specLoader;

    @BeforeEach
    public void setUp() throws Exception {
        mmc = MetaModelContext_forTesting.builder()
                        .valueSemantic(new IntValueSemantics())
                        .build();
        specLoader = mmc.getSpecificationLoader();
    }

    @ParameterizedTest
    @ValueSource(classes = {void.class, Void.class})
    void voidShouldMapToEmptyValue(final Class<?> cls) {
        var spec = specLoader.specForTypeElseFail(cls);
        assertTrue(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isValue(), ()->"isValue()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");
        assertFalse(spec.isEntityOrViewModel(), ()->"isEntityOrViewModel()");

        var emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        var emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);
    }

    @ParameterizedTest
    @ValueSource(classes = {int.class, Integer.class})
    void intShouldMapToValue(final Class<?> cls) {
        var spec = specLoader.specForTypeElseFail(cls);
        assertFalse(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isValue(), ()->"isValue()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");
        assertFalse(spec.isEntityOrViewModel(), ()->"isEntityOrViewModel()");

        var emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        var emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);

        var presentObject = ManagedObject.adaptSingular(specLoader, 3);
        assertEquals(Specialization.VALUE, presentObject.getSpecialization());

        presentObject.assertCompliance(6);

        assertThrows(AssertionError.class, ()->{
            presentObject.assertCompliance("incompatible");
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {DomainObjectList.class})
    @SneakyThrows
    void someTypesShouldMapToViewmodel(final Class<?> cls) {
        var spec = specLoader.specForTypeElseFail(cls);
        assertFalse(spec.isVoid(), ()->"isVoid()");
        assertTrue(spec.isViewModel(), ()->"isViewModel()");
        assertFalse(spec.isAbstract(), ()->"isAbstract()");
        assertFalse(spec.isInjectable(), ()->"isInjectable()");

        var emptySpez = Specialization.inferFrom(spec, null);
        assertEquals(Specialization.EMPTY, emptySpez);

        var emptyObject = ManagedObject.empty(spec);
        assertNotNull(emptyObject);

        var constructor = cls.getConstructor(_Constants.emptyClasses);
        var pojo = constructor.newInstance(_Constants.emptyObjects);

        var presentObject = ManagedObject.adaptSingular(specLoader, pojo);
        assertEquals(Specialization.VIEWMODEL, presentObject.getSpecialization());

        presentObject.assertCompliance(pojo);

        //TODO
//        assertThrows(AssertionError.class, ()->{
//            presentObject.assertCompliance("incompatible");
//        });
    }

    @Test
    void comparatorShouldNotViolateItsGeneralContract() {

        var stringSpec = specLoader.specForTypeElseFail(String.class);

        final String[] DATA = new String[] { "fn1.ln1@vendor3.example.com", "fn2.ln2@vendor4.example.com",
                "fn3.ln3@vendor6.example.com", "fn4.ln4@vendor10.example.com", "fn5.ln5@vendor6.example.com",
                "fn6.ln6@vendor4.example.com", "fn7.ln7@vendor3.example.com", "fn8.ln8@vendor4.example.com",
                "fn9.ln9@vendor9.example.com", "fn10.ln10@vendor9.example.com", "fn11.ln11@vendor8.example.com", null,
                "fn13.ln13@vendor5.example.com", "fn14.ln14@vendor8.example.com", "fn15.ln15@vendor6.example.com",
                "fn16.ln16@vendor6.example.com", "fn17.ln17@vendor4.example.com", "fn18.ln18@vendor5.example.com",
                "fn19.ln19@vendor3.example.com", "fn20.ln20@vendor3.example.com", "fn21.ln21@vendor8.example.com",
                "fn22.ln22@vendor8.example.com", "fn23.ln23@vendor4.example.com", null, "fn25.ln25@vendor9.example.com",
                "fn26.ln26@vendor10.example.com", "fn27.ln27@vendor4.example.com", "fn28.ln28@vendor9.example.com",
                "fn29.ln29@vendor8.example.com", "fn30.ln30@vendor3.example.com", "fn31.ln31@vendor4.example.com",
                "fn32.ln32@vendor4.example.com", "fn33.ln33@vendor7.example.com", "fn34.ln34@vendor10.example.com",
                "fn35.ln35@vendor7.example.com", null, "fn37.ln37@vendor7.example.com", "fn38.ln38@vendor1.example.com",
                "fn39.ln39@vendor10.example.com", "fn40.ln40@vendor5.example.com", "fn41.ln41@vendor9.example.com",
                "fn42.ln42@vendor9.example.com", "fn43.ln43@vendor5.example.com", "fn44.ln44@vendor6.example.com",
                "fn45.ln45@vendor1.example.com", "fn46.ln46@vendor6.example.com", "fn47.ln47@vendor5.example.com", null,
                "fn49.ln49@vendor4.example.com", "fn50.ln50@vendor1.example.com", "fn51.ln51@vendor8.example.com",
                "fn52.ln52@vendor3.example.com", "fn53.ln53@vendor10.example.com", "fn54.ln54@vendor5.example.com",
                "fn55.ln55@vendor4.example.com", "fn56.ln56@vendor7.example.com", "fn57.ln57@vendor4.example.com",
                "fn58.ln58@vendor5.example.com", "fn59.ln59@vendor5.example.com", null, "fn234.ln234@vendor2.example.com",
                "fn61.ln61@vendor6.example.com", "fn62.ln62@vendor2.example.com", "fn63.ln63@vendor3.example.com",
                "fn64.ln64@vendor5.example.com", "fn65.ln65@vendor6.example.com", "fn66.ln66@vendor6.example.com",
                "fn67.ln67@vendor2.example.com", "fn68.ln68@vendor4.example.com", "fn69.ln69@vendor7.example.com",
                "fn70.ln70@vendor5.example.com", "fn71.ln71@vendor8.example.com", null, "fn73.ln73@vendor3.example.com",
                "fn74.ln74@vendor4.example.com", "fn75.ln75@vendor6.example.com", "fn76.ln76@vendor4.example.com",
                "fn77.ln77@vendor2.example.com", "fn78.ln78@vendor5.example.com", "fn79.ln79@vendor5.example.com",
                "fn80.ln80@vendor9.example.com", "fn81.ln81@vendor2.example.com", "fn82.ln82@vendor10.example.com",
                "fn83.ln83@vendor3.example.com", null, "fn85.ln85@vendor9.example.com", "fn86.ln86@vendor8.example.com",
                "fn87.ln87@vendor10.example.com", "fn88.ln88@vendor7.example.com", "fn89.ln89@vendor2.example.com",
                "fn90.ln90@vendor6.example.com", "fn91.ln91@vendor10.example.com", "fn92.ln92@vendor2.example.com",
                "fn93.ln93@vendor4.example.com", "fn94.ln94@vendor7.example.com", "fn95.ln95@vendor4.example.com", null,
                "fn97.ln97@vendor7.example.com", "fn98.ln98@vendor10.example.com", "fn99.ln99@vendor2.example.com",
                "fn100.ln100@vendor5.example.com", "fn101.ln101@vendor8.example.com", "fn102.ln102@vendor4.example.com",
                "fn103.ln103@vendor8.example.com", "fn104.ln104@vendor2.example.com", "fn105.ln105@vendor3.example.com",
                "fn106.ln106@vendor10.example.com", "fn107.ln107@vendor9.example.com", null,
                "fn109.ln109@vendor7.example.com", "fn110.ln110@vendor10.example.com", null,
                "fn111.ln111@vendor1.example.com", "fn112.ln112@vendor10.example.com", "fn113.ln113@vendor8.example.com",
                "fn114.ln114@vendor9.example.com", "fn115.ln115@vendor3.example.com", "fn116.ln116@vendor6.example.com",
                "fn117.ln117@vendor2.example.com", "fn118.ln118@vendor9.example.com", "fn119.ln119@vendor9.example.com",
                null, "fn121.ln121@vendor8.example.com", "fn122.ln122@vendor3.example.com",
                "fn123.ln123@vendor8.example.com", "fn124.ln124@vendor4.example.com", "fn125.ln125@vendor5.example.com",
                "fn126.ln126@vendor2.example.com", "fn127.ln127@vendor4.example.com", "fn128.ln128@vendor1.example.com",
                "fn129.ln129@vendor4.example.com", "fn130.ln130@vendor8.example.com", "fn131.ln131@vendor10.example.com",
                null, "fn133.ln133@vendor3.example.com", "fn134.ln134@vendor8.example.com",
                "fn135.ln135@vendor4.example.com", "fn136.ln136@vendor7.example.com", "fn137.ln137@vendor1.example.com",
                "fn138.ln138@vendor3.example.com", "fn139.ln139@vendor2.example.com", "fn140.ln140@vendor5.example.com",
                "fn141.ln141@vendor9.example.com", "fn142.ln142@vendor8.example.com", "fn143.ln143@vendor10.example.com",
                null, "fn145.ln145@vendor8.example.com", "fn146.ln146@vendor10.example.com",
                "fn147.ln147@vendor3.example.com", "fn148.ln148@vendor9.example.com", "fn149.ln149@vendor2.example.com",
                "fn150.ln150@vendor3.example.com", "fn151.ln151@vendor5.example.com", "fn152.ln152@vendor5.example.com",
                "fn153.ln153@vendor6.example.com", "fn154.ln154@vendor9.example.com", "fn155.ln155@vendor4.example.com",
                null, "fn157.ln157@vendor3.example.com", "fn158.ln158@vendor9.example.com",
                "fn159.ln159@vendor8.example.com", "fn160.ln160@vendor9.example.com", "fn161.ln161@vendor5.example.com",
                "fn162.ln162@vendor10.example.com", "fn163.ln163@vendor8.example.com", "fn164.ln164@vendor6.example.com",
                "fn165.ln165@vendor2.example.com", "fn166.ln166@vendor3.example.com", "fn167.ln167@vendor7.example.com",
                "fn169.ln169@vendor8.example.com", "fn170.ln170@vendor5.example.com", "fn171.ln171@vendor4.example.com",
                "fn172.ln172@vendor5.example.com", "fn173.ln173@vendor3.example.com", "fn174.ln174@vendor8.example.com",
                "fn175.ln175@vendor2.example.com", "fn176.ln176@vendor8.example.com", "fn177.ln177@vendor7.example.com",
                "fn178.ln178@vendor9.example.com", "fn179.ln179@vendor10.example.com", null,
                "fn181.ln181@vendor6.example.com", "fn182.ln182@vendor4.example.com", "fn183.ln183@vendor2.example.com",
                "fn184.ln184@vendor3.example.com", "fn185.ln185@vendor9.example.com", "fn186.ln186@vendor3.example.com",
                "fn187.ln187@vendor9.example.com", "fn188.ln188@vendor8.example.com", "fn189.ln189@vendor2.example.com",
                "fn190.ln190@vendor10.example.com", "fn191.ln191@vendor3.example.com", null,
                "fn193.ln193@vendor9.example.com", "fn194.ln194@vendor1.example.com", "fn195.ln195@vendor7.example.com",
                "fn196.ln196@vendor3.example.com", "fn197.ln197@vendor5.example.com", "fn198.ln198@vendor2.example.com",
                "fn199.ln199@vendor4.example.com", "fn200.ln200@vendor6.example.com", "fn201.ln201@vendor4.example.com",
                "fn202.ln202@vendor1.example.com", "fn203.ln203@vendor2.example.com", null,
                "fn205.ln205@vendor10.example.com", "fn206.ln206@vendor3.example.com", "fn207.ln207@vendor5.example.com",
                "fn208.ln208@vendor6.example.com", "fn209.ln209@vendor9.example.com", "fn210.ln210@vendor5.example.com",
                "fn211.ln211@vendor9.example.com", "fn212.ln212@vendor1.example.com", "fn213.ln213@vendor1.example.com",
                "fn214.ln214@vendor6.example.com", "fn215.ln215@vendor10.example.com", null,
                "fn217.ln217@vendor6.example.com", "fn218.ln218@vendor5.example.com", "fn219.ln219@vendor8.example.com",
                "fn220.ln220@vendor4.example.com", "fn221.ln221@vendor7.example.com", "fn222.ln222@vendor2.example.com",
                "fn223.ln223@vendor8.example.com", "fn224.ln224@vendor10.example.com", "fn225.ln225@vendor10.example.com",
                "fn226.ln226@vendor6.example.com", "fn227.ln227@vendor6.example.com", null,
                "fn229.ln229@vendor1.example.com", "fn230.ln230@vendor6.example.com", "fn231.ln231@vendor5.example.com",
                "fn232.ln232@vendor3.example.com", "fn233.ln233@vendor10.example.com", "fn235.ln235@vendor7.example.com",
                "fn236.ln236@vendor4.example.com", "fn237.ln237@vendor5.example.com", "fn238.ln238@vendor7.example.com",
                "fn239.ln239@vendor5.example.com", null, "fn241.ln241@vendor5.example.com",
                "fn242.ln242@vendor1.example.com", "fn243.ln243@vendor4.example.com", "fn244.ln244@vendor10.example.com",
                "fn245.ln245@vendor3.example.com", "fn246.ln246@vendor3.example.com", "fn247.ln247@vendor3.example.com",
                "fn248.ln248@vendor3.example.com", "fn249.ln249@vendor4.example.com", "fn250.ln250@vendor8.example.com",
                "fn251.ln251@vendor10.example.com", null, "fn253.ln253@vendor8.example.com",
                "fn254.ln254@vendor6.example.com", "fn255.ln255@vendor5.example.com", "fn256.ln256@vendor2.example.com",
                "fn257.ln257@vendor1.example.com", "fn258.ln258@vendor9.example.com", "fn259.ln259@vendor6.example.com",
                "fn260.ln260@vendor3.example.com", "fn261.ln261@vendor7.example.com", "fn262.ln262@vendor10.example.com",
                "fn263.ln263@vendor1.example.com", null, "fn265.ln265@vendor10.example.com",
                "fn266.ln266@vendor3.example.com", "fn267.ln267@vendor8.example.com", "fn268.ln268@vendor9.example.com",
                "fn269.ln269@vendor8.example.com", "fn270.ln270@vendor7.example.com", "fn271.ln271@vendor1.example.com",
                "fn272.ln272@vendor3.example.com", "fn273.ln273@vendor10.example.com", "fn274.ln274@vendor7.example.com",
                "fn275.ln275@vendor8.example.com", null, "fn277.ln277@vendor4.example.com",
                "fn278.ln278@vendor5.example.com", "fn279.ln279@vendor10.example.com", "fn280.ln280@vendor2.example.com",
                "fn281.ln281@vendor2.example.com", "fn282.ln282@vendor4.example.com", "fn283.ln283@vendor2.example.com",
                "fn284.ln284@vendor3.example.com", "fn285.ln285@vendor3.example.com", "fn286.ln286@vendor5.example.com",
                "fn287.ln287@vendor7.example.com", null, "fn289.ln289@vendor8.example.com",
                "fn290.ln290@vendor1.example.com", "fn291.ln291@vendor8.example.com", "fn292.ln292@vendor1.example.com",
                "fn293.ln293@vendor3.example.com", "fn294.ln294@vendor8.example.com", "fn295.ln295@vendor7.example.com",
                "fn296.ln296@vendor4.example.com", "fn297.ln297@vendor1.example.com", "fn298.ln298@vendor7.example.com",
                "fn299.ln299@vendor9.example.com", null };

        // given
        final List<ManagedObject> managedObjects = _NullSafe.stream(DATA)
                .map(sampleString->
                    sampleString!=null
                        ? ManagedObject.value(stringSpec, sampleString)
                        : ManagedObject.empty(stringSpec))
                .collect(Collectors.toList());

        // when, then ... if broken throws java.lang.IllegalArgumentException: Comparison method violates its general contract!
        managedObjects.sort(MmSortUtils.NATURAL_NULL_FIRST);
    }

}
