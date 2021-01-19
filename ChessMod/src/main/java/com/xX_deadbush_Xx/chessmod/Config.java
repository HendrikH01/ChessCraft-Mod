package com.xX_deadbush_Xx.chessmod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static class Client {
		
		public final ForgeConfigSpec.ConfigValue<String> computername;
		
		public Client(ForgeConfigSpec.Builder builder) {
		this.computername = builder
				.comment("What should the Chess Engine be called? default: Computer", "The name should be something that can be called 'the', for example 'the computer does xy'")
				.define("computername", "Computer");		
		}
	}

	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	public static class Common {
		public final ForgeConfigSpec.BooleanValue allowHints;

		public Common(ForgeConfigSpec.Builder builder) {
			this.allowHints = builder
					.comment("Should hints be allowed? default: true")
					.define("allowhints", true);
		}
	}

	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
}