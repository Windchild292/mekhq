/*
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.universe.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.RandomOriginOptions;
import mekhq.campaign.RandomSkillPreferences;
import mekhq.campaign.universe.companyGeneration.CompanyGenerationOptions;
import mekhq.campaign.universe.generators.companyGenerators.AtBCompanyGenerator;
import mekhq.campaign.universe.generators.companyGenerators.WindchildCompanyGenerator;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompanyGenerationMethodTest {
    //region Variable Declarations
    private static final CompanyGenerationMethod[] methods = CompanyGenerationMethod.values();

    private final transient ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Universe",
            MekHQ.getMHQOptions().getLocale(), new EncodeControl());
    //endregion Variable Declarations

    //region Getters
    @Test
    public void testGetToolTipText() {
        assertEquals(resources.getString("CompanyGenerationMethod.AGAINST_THE_BOT.toolTipText"),
                CompanyGenerationMethod.AGAINST_THE_BOT.getToolTipText());
        assertEquals(resources.getString("CompanyGenerationMethod.WINDCHILD.toolTipText"),
                CompanyGenerationMethod.WINDCHILD.getToolTipText());
    }
    //endregion Getters

    //region Boolean Comparison Methods
    @Test
    public void testIsAgainstTheBot() {
        for (final CompanyGenerationMethod companyGenerationMethod : methods) {
            if (companyGenerationMethod == CompanyGenerationMethod.AGAINST_THE_BOT) {
                assertTrue(companyGenerationMethod.isAgainstTheBot());
            } else {
                assertFalse(companyGenerationMethod.isAgainstTheBot());
            }
        }
    }

    @Test
    public void testIsWindchild() {
        for (final CompanyGenerationMethod companyGenerationMethod : methods) {
            if (companyGenerationMethod == CompanyGenerationMethod.WINDCHILD) {
                assertTrue(companyGenerationMethod.isWindchild());
            } else {
                assertFalse(companyGenerationMethod.isWindchild());
            }
        }
    }
    //region Boolean Comparison Methods

    @Test
    public void testGetGenerator() {
        final Campaign mockCampaign = mock(Campaign.class);
        when(mockCampaign.getPersonnelGenerator(any(), any())).thenCallRealMethod();
        when(mockCampaign.getRandomSkillPreferences()).thenReturn(mock(RandomSkillPreferences.class));

        final CompanyGenerationOptions mockOptions = mock(CompanyGenerationOptions.class);
        when(mockOptions.getRandomOriginOptions()).thenReturn(new RandomOriginOptions(false));
        when(mockOptions.getBattleMechWeightClassGenerationMethod()).thenReturn(BattleMechWeightClassGenerationMethod.WINDCHILD);
        when(mockOptions.getBattleMechQualityGenerationMethod()).thenReturn(BattleMechQualityGenerationMethod.WINDCHILD);

        assertInstanceOf(AtBCompanyGenerator.class, CompanyGenerationMethod.AGAINST_THE_BOT.getGenerator(mockCampaign, mockOptions));
        assertInstanceOf(WindchildCompanyGenerator.class, CompanyGenerationMethod.WINDCHILD.getGenerator(mockCampaign, mockOptions));
    }

    @Test
    public void testToStringOverride() {
        assertEquals(resources.getString("CompanyGenerationMethod.AGAINST_THE_BOT.text"),
                CompanyGenerationMethod.AGAINST_THE_BOT.toString());
        assertEquals(resources.getString("CompanyGenerationMethod.WINDCHILD.text"),
                CompanyGenerationMethod.WINDCHILD.toString());
    }
}
