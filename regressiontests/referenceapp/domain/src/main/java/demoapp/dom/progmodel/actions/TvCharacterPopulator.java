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
package demoapp.dom.progmodel.actions;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class TvCharacterPopulator {

    public void populate(Set<TvCharacter> tvCharacters) {
        tvCharacters.clear();
        tvCharacters.add(TvCharacter.of("Tom", TvShow.THE_GOOD_LIFE, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Barbara", TvShow.THE_GOOD_LIFE, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Jerry", TvShow.THE_GOOD_LIFE, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Margo", TvShow.THE_GOOD_LIFE, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Joey", TvShow.FRIENDS, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Monica", TvShow.FRIENDS, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Rachel", TvShow.FRIENDS, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Phoebe", TvShow.FRIENDS, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Chandler", TvShow.FRIENDS, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Ross", TvShow.FRIENDS, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Mary", TvShow.MMM, TvCharacter.Sex.FEMALE));
        tvCharacters.add(TvCharacter.of("Mungo", TvShow.MMM, TvCharacter.Sex.MALE));
        tvCharacters.add(TvCharacter.of("Midge", TvShow.MMM, TvCharacter.Sex.MALE));
    }
}

