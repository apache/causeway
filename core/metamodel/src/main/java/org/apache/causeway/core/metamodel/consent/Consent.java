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
package org.apache.causeway.core.metamodel.consent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;

import lombok.NonNull;
import lombok.experimental.Accessors;

public interface Consent {

    //XXX record candidate
    @lombok.Value @Accessors(fluent=true)
    public static class VetoReason implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Introduced to help pick a winner when merging 2 {@link VetoReason}(s).
         * <p>
         * PROGRAMMING_MODEL always wins over SECURITY.
         */
        public enum VetoOriging {
            /**
             * Veto originates from security mechanism.
             * <p>
             * In other words: the user is not authorized to either view or change the feature.
             */
            SECURITY,
            /**
             * Veto originates from programming model, either implicit or explicit see {@link UiHint}.
             */
            PROGRAMMING_MODEL;
            public boolean isSecurity() { return this==SECURITY; }
            public boolean isProgrammingModel() { return this==PROGRAMMING_MODEL; }
            /** reduce to max ordinal */
            static VetoOriging reduceToMaxOrdinal(final VetoOriging a, final VetoOriging b) {
                return b.ordinal()>a.ordinal() ? b : a;
            }
        }
        /**
         * Introduced to help decide whether or not to display a 'ban' icon
         * in the UI, with a tooltip showing the disabled reason.
         */
        public enum UiHint {
            /**
             * Reason is <b>implicit</b> by programming model,
             * reason text is generic and rather of interest to the developer
             * than the end-user.
             * <p>
             * Show only when prototyping. However, when prototyping, can be suppressed via config option.
             */
            NO_ICON_UNLESS_PROTOTYPING,
            /**
             * Reason is <b>explicit</b> either by programming model or authentication mechanism,
             * reason text is designated to reach the end-user.
             */
            SHOW_BAN_ICON;
            public boolean isNoIconUnlessPrototying() { return this==NO_ICON_UNLESS_PROTOTYPING; }
            public boolean isShowBanIcon() { return this==SHOW_BAN_ICON; }
            /** reduce to max ordinal */
            static UiHint reduceToMaxOrdinal(final UiHint a, final UiHint b) {
                return b.ordinal()>a.ordinal() ? b : a;
            }
        }
        private final VetoOriging vetoOriging;
        private final UiHint uiHint;
        private final @NonNull String string;
        private static VetoReason of(
                final @NonNull VetoOriging vetoOriging,
                final @NonNull UiHint uiHint,
                final String reason) {
            _Assert.assertTrue(_Strings.isNotEmpty(reason));
            return new VetoReason(vetoOriging, uiHint, reason);
        }
        public static VetoReason unauthorized(final String reason) {
            return of(VetoOriging.SECURITY, UiHint.SHOW_BAN_ICON, reason);
        }
        public static VetoReason explicit(final String reason) {
            return of(VetoOriging.PROGRAMMING_MODEL, UiHint.SHOW_BAN_ICON, reason);
        }
        private static VetoReason inferred(final String reason) {
            return of(VetoOriging.PROGRAMMING_MODEL, UiHint.NO_ICON_UNLESS_PROTOTYPING, reason);
        }

        VetoReason withAdvisorAsDiagnostic(Object advisor) {
            return new VetoReason(this.vetoOriging, this.uiHint, this.string + " {" + advisor.getClass().getSimpleName() + "} ");
        }

        public Optional<VetoReason> toOptional() {
            return Optional.of(this);
        }
        /**
         * {@code Pi}: origin=PROGRAMMING_MODEL, nature=implicit (NO_ICON_UNLESS_PROTOTYPING)<br>
         * {@code Pe}: origin=PROGRAMMING_MODEL, nature=explicit (SHOW_BAN_ICON)<br>
         * {@code S(e)}: origin=SECURITY, nature=explicit (SHOW_BAN_ICON)<br>
         * <p>
         * {@code Pi ৹ Pi := pick any or concat}<br>
         * {@code Pe ৹ Pe := pick any or concat}<br>
         * {@code S(e) ৹ S(e) := pick any or concat}<br>
         * <br>
         * {@code Pi ৹ Pe := Pe}<br>
         * {@code Pi ৹ S(e) := Pi}<br>
         * {@code Pe ৹ S(e) := Pe}<br>
         * In other words: winner picking is driven by <i>origin</i> first and <i>nature</i> second.
         */
        public VetoReason reduce(final VetoReason other) {
            // arbitrarily shifting left by 4 so reserving some room for more UiHint enum values
            final int thisScore = (this.vetoOriging.ordinal()<<4)  + this.uiHint.ordinal();
            final int otherScore = (other.vetoOriging.ordinal()<<4)  + other.uiHint.ordinal();

            final List<String> mergedText = new ArrayList<>(2);
            final List<UiHint> winnerUiHint = new ArrayList<>(1);
            switch(_Ints.compare(thisScore, otherScore)) {
            case -1: winnerUiHint.add(other.uiHint); mergedText.add(other.string); break;
            case 0:  winnerUiHint.add(this.uiHint); mergedText.add(this.string); mergedText.add(other.string); break;
            case 1:  winnerUiHint.add(this.uiHint); mergedText.add(this.string); break;
            }
            return new VetoReason(
                    VetoOriging.reduceToMaxOrdinal(this.vetoOriging, other.vetoOriging),
                    winnerUiHint.get(0),
                    mergedText.stream().collect(Collectors.joining("; ")));
        }
        // -- PREDEFINED REASONS
        public static VetoReason editingObjectDisabledReasonNotGiven() {
            return VetoReason.inferred("Disabled via @DomainObject annotation, reason not given.");
        }
        public static VetoReason editingPropertyDisabledReasonNotGiven() {
            return VetoReason.inferred("Disabled via @Property annotation, reason not given.");
        }
        public static VetoReason propertyHasNoSetter() {
            return VetoReason.inferred("Disabled, property has no setter.");
        }
        public static VetoReason bounded() {
            return VetoReason.inferred("Cannot edit a bounded member.");
        }
        public static VetoReason immutableValueType() {
            return VetoReason.inferred("Value types are immutable.");
        }
        public static VetoReason immutablePrimaryKey() {
            return VetoReason.inferred("Primary-keys are immutable.");
        }
        public static VetoReason mixedinCollection() {
            return inferred("Cannot edit a mixed-in collection.");
        }
        public static VetoReason mixedinProperty() {
            return inferred("Cannot edit a mixed-in property.");
        }
        public static VetoReason immutableIfNoReasonGivenByImmutableFacet() {
            return inferred("Immutable, no reason given by ImmutableFacet.");
        }
        public static VetoReason delegatedTo(@NonNull final Class<? extends ImmutableFacet> cls) {
            return inferred("Calculated at runtime, delegating to ImmutableFacet " + cls + ".");
        }
    }

    /**
     * Returns true if this object is giving permission.
     */
    boolean isAllowed();

    /**
     * Returns true if this object is NOT giving permission.
     */
    boolean isVetoed();

    /**
     * Optionally the {@link VetoReason}, why consent is being vetoed, based on whether not allowed.
     * <p>
     * Will correspond to the {@link InteractionResult#getReason() reason} in
     * the contained {@link #getInteractionResult() InteractionResult} (if one
     * was specified).
     */
    Optional<VetoReason> getReason();

    /**
     * Optionally the {@link VetoReason} as String, why consent is being vetoed, based on whether not allowed.
     */
    default Optional<String> getReasonAsString() {
        return getReason().map(VetoReason::string);
    }

    /**
     * Description of the interaction that this consent represents.
     *
     * <p>
     * May be <tt>null</tt>.
     */
    String getDescription();

    /**
     * Allows the description of the interaction to which this consent relates
     * to be specified or refined.
     *
     * @param description
     * @return this consent
     */
    Consent setDescription(String description);

    /**
     * The {@link InteractionResult} that created this {@link Consent}.
     *
     * @return - may be <tt>null</tt> if created as a legacy {@link Consent}.
     *
     */
    public InteractionResult getInteractionResult();

}
