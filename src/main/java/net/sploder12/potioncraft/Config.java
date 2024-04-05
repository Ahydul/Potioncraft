package net.sploder12.potioncraft;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Config {
    static final String filename = "potioncraft/potioncraft";

    private static File configFile = null;

    // Config Fields

    static boolean debug = false;
    static final String debugStr = "debug";

    static boolean allowMixing = true;
    static final String allowMixingStr = "allow_mixing";

    static boolean canUseReagents = true;
    static final String canUseReagentsStr = "can_use_reagents";

    static float potencyUpChanceNumEffectsBy = 0.3f;
    static final String potencyUpChanceNumEffectsByStr = "potencyUpChanceNumEffectsBy";

    static float decreaseDurationPerNumEffectsBy = 0.25f;
    static final String decreaseDurationPerNumEffectsByStr = "decreaseDurationPerNumEffectsBy";

    static float dilutionFactor = 0.2f;
    static final String dilutionFactorStr = "dilutionFactor";

    public record Pair(float first, float second) { }
    static Map<StatusEffect, Pair> effectConfig = new HashMap<StatusEffect, Pair>();
    static final String effectConfigStr = "effectConfig";
    
    static void loadDefaults() {
        debug = false;
        allowMixing = true;
        canUseReagents = true;
        potencyUpChanceNumEffectsBy = 0.3f;
        decreaseDurationPerNumEffectsBy = 0.25f;
        dilutionFactor = 0.2f;
        loadEffectConfig();
    }

    private static void loadEffectConfig(){
        //Speed, strength, instant health, instant damage, jump boost, fire resistance, water breathing, invisibility, night vision, health boost
        IntStream.of(1,5,6,7,8,12,13,14,16,21).forEach(i -> 
            effectConfig.put(Registries.STATUS_EFFECT.get(i), new Pair(480f, 3f))
        );
        
        effectConfig.put(Registries.STATUS_EFFECT.get(2), new Pair(240f, 4f)); //Slowness
        effectConfig.put(Registries.STATUS_EFFECT.get(3), new Pair(120f, 3f)); //Haste
        effectConfig.put(Registries.STATUS_EFFECT.get(4), new Pair(300f, 4f)); //Mining Fatigue
        effectConfig.put(Registries.STATUS_EFFECT.get(9), new Pair(30f, 1f)); //Nausea
        effectConfig.put(Registries.STATUS_EFFECT.get(10), new Pair(120f, 3f)); //Regeneration
        effectConfig.put(Registries.STATUS_EFFECT.get(11), new Pair(120f, 3f)); //Resistance
        effectConfig.put(Registries.STATUS_EFFECT.get(15), new Pair(120f, 1f)); //Blindness
        effectConfig.put(Registries.STATUS_EFFECT.get(17), new Pair(60f, 3f)); //Hunger
        effectConfig.put(Registries.STATUS_EFFECT.get(18), new Pair(240f, 2f)); //Weakness
        effectConfig.put(Registries.STATUS_EFFECT.get(19), new Pair(120f, 3f)); //Poison
        effectConfig.put(Registries.STATUS_EFFECT.get(20), new Pair(60f, 3f)); //Wither
        effectConfig.put(Registries.STATUS_EFFECT.get(22), new Pair(120f, 4f)); //Absorption
        effectConfig.put(Registries.STATUS_EFFECT.get(24), new Pair(120f, 1f)); //Glowing
        effectConfig.put(Registries.STATUS_EFFECT.get(25), new Pair(20f, 2f)); //Levitation
        effectConfig.put(Registries.STATUS_EFFECT.get(28), new Pair(240f, 1f)); //Slow falling
        effectConfig.put(Registries.STATUS_EFFECT.get(30), new Pair(120f, 2f)); //Dolphins grace
        effectConfig.put(Registries.STATUS_EFFECT.get(33), new Pair(60f, 1f)); //Darkness
    }

    static void resetConfig() {
        loadDefaults();

        try {
            saveConfig();
        }
        catch (IOException e) {
            Main.log("Config failed to save! " + e);
        }
    }


    static void loadConfig() {
        if (configFile == null) {
            Path path = FabricLoader.getInstance().getConfigDir();
            configFile = path.resolve(filename + ".properties").toFile();
        }

        // raw config data
        HashMap<String, String> config = new HashMap<>();

        loadDefaults();

        try (Scanner ifstream = new Scanner(configFile)) {
            while(ifstream.hasNextLine()) {
                String line = ifstream.nextLine();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] data = line.split("=", 2);
                if (data.length >= 2) {
                    config.put(data[0], data[1]);
                } // ignore length 1 data
            }
        }
        catch (FileNotFoundException e) {
            resetConfig();
            return;
        }

        boolean containsAll = true;

        // read and parse all data from config
        if (config.containsKey(debugStr)) {
           debug = Boolean.parseBoolean(config.get(debugStr));
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(allowMixingStr)) {
            allowMixing = Boolean.parseBoolean(config.get(allowMixingStr));
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(canUseReagentsStr)) {
            canUseReagents = Boolean.parseBoolean(config.get(canUseReagentsStr));
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(potencyUpChanceNumEffectsByStr)) {
            float chance = Float.parseFloat(config.get(potencyUpChanceNumEffectsByStr));
            if (chance < 0 || chance > 1) {
                containsAll = false;
            }
            else{
                potencyUpChanceNumEffectsBy = chance;
            }
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(decreaseDurationPerNumEffectsByStr)) {
            decreaseDurationPerNumEffectsBy = Math.max(Float.parseFloat(config.get(decreaseDurationPerNumEffectsByStr)), 0);
        }
        else {
            containsAll = false;
        }
        
        if (config.containsKey(dilutionFactorStr)) {
            dilutionFactor = Math.max(Math.min(Float.parseFloat(config.get(dilutionFactorStr)), 1),0);
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(effectConfigStr)) {
            String[] spl = config.get(effectConfigStr).replaceAll(" ", "").split("|");
            for (String s : spl) {
                String[] spl2 = s.split(",");
                try {
                    effectConfig.put(
                        Registries.STATUS_EFFECT.get(Integer.parseInt(spl2[0])),
                        new Pair(
                            Float.parseFloat(spl2[1]),
                            Float.parseFloat(spl2[2])
                        )
                    );
                } catch (Exception e) {
                    containsAll = false;
                    break;
                }
            }
        }
        else {
            containsAll = false;
        }
        
        // config is missing some properties, probably out of date
        if (!containsAll) {
            try {
                saveConfig();
            }
            catch (IOException e) {
                Main.log("Config failed to save! " + e);
            }
        }
    }

    private static FileWriter writeBool(FileWriter writer, boolean bool) throws IOException {
        if (bool) {
            writer.write("true\n\n");
        }
        else {
            writer.write("false\n\n");
        }
        return writer;
    }


    static void saveConfig() throws IOException {
        if (configFile == null) {
            Path path = FabricLoader.getInstance().getConfigDir();
            configFile = path.resolve(filename + ".properties").toFile();
        }

        configFile.getParentFile().mkdirs();
        configFile.createNewFile();

        try (FileWriter ofstream = new FileWriter(configFile)) {

            ofstream.write("#Potioncraft Config\n");
            ofstream.write("#Timestamp: ");
            ofstream.write(LocalDateTime.now() + "\n\n");

            ofstream.write("#Should Potion Mixing be Possible?\n");
            ofstream.write(allowMixingStr + '=');
            writeBool(ofstream, allowMixing);

            ofstream.write("#Should Adding Reagents to Mixtures be Possible?\n");
            ofstream.write(canUseReagentsStr + '=');
            writeBool(ofstream, canUseReagents);

            ofstream.write("#Chance that glowstone wont make the potion have a higher potency per number of effects in cauldron\n");
            ofstream.write(potencyUpChanceNumEffectsByStr + '=' + potencyUpChanceNumEffectsBy + '\n');

            ofstream.write("#How much maxDuration is decreased per number of extra (more than 1) effects in cauldron . 0 to disable\n");
            ofstream.write(decreaseDurationPerNumEffectsByStr + '=' + decreaseDurationPerNumEffectsBy + '\n');
            
            ofstream.write("#Factor of lost effect when adding more effects or diluting cauldron. 1 to disable\n");
            ofstream.write(dilutionFactorStr + '=' + dilutionFactor + '\n');

            ofstream.write("#List of effect_id max_duration max_potency_Level \n");
            ofstream.write(effectConfig.entrySet().stream()
                .map(e -> StatusEffect.getRawId(e.getKey()) + ", "+ e.getValue().first+ ", "+ e.getValue().second)
                .collect(Collectors.joining(" | ")) + '\n'
            );
        }
    }


}
