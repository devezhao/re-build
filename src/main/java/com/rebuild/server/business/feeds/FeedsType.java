/*
rebuild - Building your business-systems freely.
Copyright (C) 2019 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.server.business.feeds;

/**
 * 动态类型
 *
 * @author devezhao
 * @since 2019/11/1
 */
public enum FeedsType {

    ACTIVITY(1, "动态"),
    FOLLOWUP(2, "跟进"),
    ANNOUNCEMENT(3, "公告"),

    ;

    final private int mask;
    final private String name;

    FeedsType(int mask, String name) {
        this.mask = mask;
        this.name = name;
    }

    /**
     * @return
     */
    public int getMask() {
        return mask;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param typeMask
     * @return
     */
    public static FeedsType parse(int typeMask) {
        for (FeedsType t : values()) {
            if (t.getMask() == typeMask) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknow mask : " + typeMask);
    }
}
