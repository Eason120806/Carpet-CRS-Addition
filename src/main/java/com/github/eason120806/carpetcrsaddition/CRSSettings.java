/*
 * This file is part of the Carpet CRS Addition project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2025  Eason120806 and contributors
 *
 * Carpet CRS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet CRS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Carpet CRS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.eason120806.carpetcrsaddition;

import carpet.api.settings.Rule;

@SuppressWarnings("unused")
public class CRSSettings {
    private static final String CRS = "CRS";
    private static final String PORTING = "Porting";
    private static final String CREATIVE = "Creative";

    @Rule(categories = {CRS, PORTING})
    public static boolean UseV1212ProjectileLogic = false;

    @Rule(categories = {CRS, PORTING})
    public static boolean PearlCanLoadingChunks = false;

    @Rule(categories = {CRS, CREATIVE})
    public static boolean DragonAlwaysDropsMaxExperience = false;
}