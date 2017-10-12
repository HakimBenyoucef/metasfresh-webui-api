package de.metas.ui.web.pickingslot.process;

import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;

import com.google.common.base.Preconditions;

import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.picking.IHUPickingSlotBL;
import de.metas.process.IProcessPrecondition;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.ui.web.picking.pickingslot.PickingSlotRow;
import de.metas.ui.web.pickingslot.AggregationPickingSlotView;
import de.metas.ui.web.process.adprocess.ViewBasedProcessTemplate;
import de.metas.ui.web.window.datatypes.DocumentIdsSelection;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class WEBUI_PickingSlot_TakeOutHU extends ViewBasedProcessTemplate implements IProcessPrecondition
{

	@Override
	protected ProcessPreconditionsResolution checkPreconditionsApplicable()
	{
		final DocumentIdsSelection selectedRowIds = getSelectedDocumentIds();
		if (selectedRowIds.isEmpty())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}
		if (!selectedRowIds.isSingleDocumentId())
		{
			return ProcessPreconditionsResolution.rejectBecauseNotSingleSelection();
		}

		final PickingSlotRow row = PickingSlotRow.cast(getSingleSelectedRow());
		if (!row.isTopLevelHU())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("select a top level HU");
		}

		return ProcessPreconditionsResolution.accept();
	}

	@Override
	protected String doIt()
	{
		final PickingSlotRow row = PickingSlotRow.cast(getSingleSelectedRow());
		Preconditions.checkState(row.isTopLevelHU(), "row %s shall be a top level HU", row);
		final I_M_HU hu = InterfaceWrapperHelper.load(row.getHuId(), I_M_HU.class);

		Services.get(IHUPickingSlotBL.class).removeFromPickingSlotQueueRecursivelly(hu);
		// TODO: move it to afterPicking locator

		final AggregationPickingSlotView pickingSlotsView = getView(AggregationPickingSlotView.class);
		invalidateView(pickingSlotsView.getViewId());
		invalidateView(pickingSlotsView.getIncludedViewId());

		return MSG_OK;
	}

}