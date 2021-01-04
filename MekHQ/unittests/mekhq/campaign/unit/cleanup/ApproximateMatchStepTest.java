package mekhq.campaign.unit.cleanup;

import static org.mockito.Mockito.*;

import org.junit.Test;

import megamek.common.AmmoType;
import megamek.common.EquipmentType;
import megamek.common.Mounted;
import mekhq.campaign.parts.equipment.AmmoBin;
import mekhq.campaign.parts.equipment.EquipmentPart;

public class ApproximateMatchStepTest {
    @Test
    public void notAmmoBinTest() {
        EquipmentProposal mockProposal = mock(EquipmentProposal.class);
        EquipmentPart mockPart = mock(EquipmentPart.class);

        ApproximateMatchStep step = new ApproximateMatchStep();

        step.visit(mockProposal, mockPart);

        verify(mockProposal, times(0)).propose(any(), anyInt(), any());
    }

    @Test
    public void noMatchingEquipmentTest() {
        EquipmentProposal mockProposal = mock(EquipmentProposal.class);
        AmmoBin mockPart = mock(AmmoBin.class);

        ApproximateMatchStep step = new ApproximateMatchStep();

        step.visit(mockProposal, mockPart);

        verify(mockProposal, times(0)).propose(any(), anyInt(), any());
    }

    @Test
    public void mountDoesNotMatchEquipmentTest() {
        EquipmentProposal mockProposal = mock(EquipmentProposal.class);
        Mounted mockMount = mock(Mounted.class);
        when(mockMount.getType()).thenReturn(mock(EquipmentType.class));
        doReturn(mockMount).when(mockProposal).getEquipment(eq(1));
        AmmoBin mockPart = mock(AmmoBin.class);
        when(mockPart.getEquipmentNum()).thenReturn(1);
        when(mockPart.getType()).thenReturn(mock(AmmoType.class));

        ApproximateMatchStep step = new ApproximateMatchStep();

        step.visit(mockProposal, mockPart);

        verify(mockProposal, times(0)).propose(any(), anyInt(), any());
    }

    @Test
    public void mountDoesNotMatchAmmoTypeTest() {
        EquipmentProposal mockProposal = mock(EquipmentProposal.class);
        Mounted mockMount = mock(Mounted.class);
        when(mockMount.getType()).thenReturn(mock(AmmoType.class));
        doReturn(mockMount).when(mockProposal).getEquipment(eq(1));
        AmmoBin mockPart = mock(AmmoBin.class);
        when(mockPart.getEquipmentNum()).thenReturn(1);
        when(mockPart.getType()).thenReturn(mock(AmmoType.class));

        ApproximateMatchStep step = new ApproximateMatchStep();

        step.visit(mockProposal, mockPart);

        verify(mockProposal, times(0)).propose(any(), anyInt(), any());
    }

    @Test
    public void mountMatchesEquipmentTest() {
        EquipmentProposal mockProposal = mock(EquipmentProposal.class);
        AmmoType mockType = mock(AmmoType.class);
        Mounted mockMount = mock(Mounted.class);
        when(mockMount.getType()).thenReturn(mockType);
        doReturn(mockMount).when(mockProposal).getEquipment(eq(1));
        AmmoBin mockPart = mock(AmmoBin.class);
        when(mockPart.getEquipmentNum()).thenReturn(1);
        when(mockPart.getType()).thenReturn(mock(AmmoType.class));
        doReturn(true).when(mockPart).canChangeMunitions(eq(mockType));

        ApproximateMatchStep step = new ApproximateMatchStep();

        step.visit(mockProposal, mockPart);

        verify(mockProposal, times(1)).propose(eq(mockPart), eq(1), eq(mockMount));
    }
}
