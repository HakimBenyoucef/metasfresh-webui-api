package de.metas.ui.web.picking;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.adempiere.util.Services;
import org.compiere.util.Env;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

import de.metas.process.IADProcessDAO;
import de.metas.process.RelatedProcessDescriptor;
import de.metas.ui.web.document.filter.DocumentFilterDescriptor;
import de.metas.ui.web.picking.process.WEBUI_Picking_AddHUToPickingSlot;
import de.metas.ui.web.picking.process.WEBUI_Picking_M_Picking_Candidate_Processed;
import de.metas.ui.web.picking.process.WEBUI_Picking_OpenHUsToPick;
import de.metas.ui.web.picking.process.WEBUI_Picking_PickToExistingHU;
import de.metas.ui.web.picking.process.WEBUI_Picking_PickToNewHU;
import de.metas.ui.web.picking.process.WEBUI_Picking_RemoveHUFromPickingSlot;
import de.metas.ui.web.view.CreateViewRequest;
import de.metas.ui.web.view.IViewFactory;
import de.metas.ui.web.view.ViewFactory;
import de.metas.ui.web.view.ViewId;
import de.metas.ui.web.view.descriptor.ViewLayout;
import de.metas.ui.web.view.json.JSONViewDataType;
import de.metas.ui.web.window.datatypes.DocumentId;
import de.metas.ui.web.window.datatypes.WindowId;
import lombok.NonNull;

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

@ViewFactory(windowId = PickingConstants.WINDOWID_PickingSlotView_String, viewTypes = { JSONViewDataType.grid, JSONViewDataType.includedView })
public class PickingSlotViewFactory implements IViewFactory
{
	@Autowired
	private PickingSlotViewRepository pickingSlotRepo;

	@Override
	public ViewLayout getViewLayout(final WindowId windowId, final JSONViewDataType viewDataType)
	{
		// TODO: cache it

		return ViewLayout.builder()
				.setWindowId(PickingConstants.WINDOWID_PickingSlotView)
				.setCaption("Picking slots")
				.addElementsFromViewRowClass(PickingSlotRow.class, viewDataType)
				.setHasTreeSupport(true)
				.build();
	}

	@Override
	public Collection<DocumentFilterDescriptor> getViewFilterDescriptors(final WindowId windowId, final JSONViewDataType viewDataType)
	{
		return getViewLayout(windowId, viewDataType).getFilters();
	}

	/**
	 * This method is called once for each shipment schedule (left-hand side) and creates the respective picking view (right-hand side)
	 */
	@Override
	public PickingSlotView createView(@NonNull final CreateViewRequest request)
	{
		final ViewId pickingViewId = request.getParentViewId();
		final DocumentId pickingRowId = request.getParentRowId();
		final ViewId pickingSlotViewId = PickingSlotViewsIndexStorage.createViewId(pickingViewId, pickingRowId);

		final int shipmentScheduleId = pickingRowId.toInt(); // TODO make it more obvious/explicit
		
		final PickingSlotRepoQuery query = PickingSlotRepoQuery.of(shipmentScheduleId);

		// notice for noobs such as me: this is executed each time the view is revalidated. and it's not executed when this createView method runs
		final Supplier<List<PickingSlotRow>> rowsSupplier = 
				() -> pickingSlotRepo.retrieveRowsByShipmentScheduleId(query);

		return PickingSlotView.builder()
				.viewId(pickingSlotViewId)
				.parentViewId(pickingViewId)
				.parentRowId(pickingRowId)
				.shipmentScheduleId(shipmentScheduleId)
				.rows(rowsSupplier)
				.additionalRelatedProcessDescriptors(createAdditionalRelatedProcessDescriptors())
				.build();
	}

	private List<RelatedProcessDescriptor> createAdditionalRelatedProcessDescriptors()
	{
		// TODO: cache it

		final IADProcessDAO adProcessDAO = Services.get(IADProcessDAO.class);
		final Properties ctx = Env.getCtx();

		return ImmutableList.of(
				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClassIfUnique(ctx, WEBUI_Picking_PickToNewHU.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build(),

				// TODO: remove WEBUI_Picking_AddHUToPickingSlot when opening included view is fixed in frontend
				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClass(ctx, WEBUI_Picking_AddHUToPickingSlot.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build(),
				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClass(ctx, WEBUI_Picking_OpenHUsToPick.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build(),

				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClass(ctx, WEBUI_Picking_RemoveHUFromPickingSlot.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build(),

				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClass(ctx, WEBUI_Picking_PickToExistingHU.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build(),

				RelatedProcessDescriptor.builder()
						.processId(adProcessDAO.retriveProcessIdByClass(ctx, WEBUI_Picking_M_Picking_Candidate_Processed.class))
						.anyTable().anyWindow()
						.webuiQuickAction(true)
						.build());
	}
}
