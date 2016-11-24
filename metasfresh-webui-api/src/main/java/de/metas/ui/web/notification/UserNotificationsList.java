package de.metas.ui.web.notification;

import java.util.List;

import com.google.common.collect.ImmutableList;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public final class UserNotificationsList
{
	/* package */static UserNotificationsList of(final List<UserNotification> notifications, final int totalCount)
	{
		if (notifications.isEmpty() && totalCount <= 0)
		{
			return EMPTY;
		}

		return new UserNotificationsList(notifications, totalCount);
	}

	public static final UserNotificationsList EMPTY = new UserNotificationsList(ImmutableList.of(), 0);

	private final List<UserNotification> notifications;
	private final int totalCount;

	private UserNotificationsList(final List<UserNotification> notifications, final int totalCount)
	{
		super();
		this.notifications = ImmutableList.copyOf(notifications);
		this.totalCount = totalCount;
	}

	public List<UserNotification> getNotifications()
	{
		return notifications;
	}

	public int getTotalCount()
	{
		return totalCount;
	}

	public boolean isEmpty()
	{
		return this == EMPTY;
	}
}