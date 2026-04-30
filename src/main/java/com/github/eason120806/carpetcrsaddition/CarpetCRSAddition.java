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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetCRSAddition implements ModInitializer {
	public static final String MOD_ID = "carpet-crs-addition";
	public static final String MOD_NAME = "Carpet CRS Addition";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static String version;

	@Override
	public void onInitialize() {
		version = FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.orElseThrow(RuntimeException::new)
				.getMetadata()
				.getVersion()
				.getFriendlyString();
		CRSExtension.init();
		LOGGER.info("[CRS] Carpet CRS Addition v{} initialized!", version);
	}
}