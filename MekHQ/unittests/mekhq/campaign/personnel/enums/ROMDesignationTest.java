package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.PersonnelTestUtilities;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;

/**
 * This class is used to test the {@link ROMDesignation} Enum
 */
public class ROMDesignationTest {
    private static final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
            new EncodeControl());

    @Test
    public void testGetComStarBranchDesignation() {

    }

    /**
     * Not sure how to do this one yet
     */
    @Test
    public void testDetermineDesignationFromRole() {
        Person mockPerson = PersonnelTestUtilities.initPerson();
    }

    /**
     * Testing to ensure the toString Override is working as intended
     */
    @Test
    public void testToStringOverride() {
        assertEquals(resources.getString("ROMDesignation.NONE.text"), ROMDesignation.NONE.toString());
        assertEquals(resources.getString("ROMDesignation.THETA.text"), ROMDesignation.THETA.toString());
        assertEquals(resources.getString("ROMDesignation.PSI.text"), ROMDesignation.PSI.toString());
    }

    /**
     * Testing to ensure the enum is properly parsed from a given String, dependant on whether it is
     * is parsing from {@link ROMDesignation}.name(), the ordinal (formerly magic numbers), or a failure
     * condition
     */
    @Test
    public void testParseFromString() {
        // Enum.valueOf Testing
        assertEquals(ROMDesignation.NONE, ROMDesignation.parseFromString("NONE"));
        assertEquals(ROMDesignation.OMICRON, ROMDesignation.parseFromString("OMICRON"));

        // Parsing from ordinal testing
        assertEquals(ROMDesignation.NONE, ROMDesignation.parseFromString("0"));
        assertEquals(ROMDesignation.IOTA, ROMDesignation.parseFromString("3"));
        assertEquals(ROMDesignation.GAMMA, ROMDesignation.parseFromString("13"));
        // This is an out of bounds check, as any future additions (albeit highly improbably)
        // must adjust for the fact that the old ordinal numbers only went up to 13
        assertEquals(ROMDesignation.NONE, ROMDesignation.parseFromString("14"));

        // Default Failure Case
        assertEquals(ROMDesignation.NONE, ROMDesignation.parseFromString("failureFailsFake"));
    }
}
