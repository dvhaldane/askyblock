package com.wasteofplastic.askyblock.util;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

/**
 * Helper class for Economy
 */
public class EconomyHelper {

	public static EconomyService economyService;

	/*
	 * Listens for economy
	 */
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
			economyService = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}

	public static BigDecimal getBalance(Player player) {
		return getBalance(player.getUniqueId());
	}

	public static BigDecimal getBalance(UUID uuid) {
		Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(uuid);
		if (uOpt.isPresent()) {
			UniqueAccount acc = uOpt.get();
			return acc.getBalance(economyService.getDefaultCurrency());
		}
		return null;
	}

	public static boolean hasBalance(Player player, BigDecimal moneyRequired) {
		if (hasBalance(player, Double.valueOf(moneyRequired.toString())))
			return true;
		return false;
	}

	public static boolean hasBalance(Player player, double moneyRequired) {
		if (Double.valueOf(getBalance(player).toString()) >= moneyRequired)
			return true;
		return false;
	}

}
