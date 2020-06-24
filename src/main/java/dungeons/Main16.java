package dungeons;


import dungeons.kaptainwutax.magic.PopulationReversal;
import dungeons.kaptainwutax.magic.RandomSeed;
import dungeons.kaptainwutax.util.LCG;
import dungeons.kaptainwutax.util.Rand;
import randomreverser.ReverserDevice;
import randomreverser.call.FilteredSkip;
import randomreverser.call.NextInt;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Main16 {


    public static void main(String[] args) {
        //============================================================ START INPUT
        Scanner in = new Scanner(System.in);
        System.out.println("Enter posX of spawner");
        int posX = in.nextInt();
        System.out.println("Enter posY of spawner");
        int posY = in.nextInt();
        System.out.println("Enter posZ of spawner, read it ");
        int posZ = in.nextInt();
        System.out.println("Enter the sequence, Read it from the image with the supplied script");
        String stringPattern = in.nextLine();
        stringPattern = in.nextLine();
        //int posX = -1840;
        //int posY = 23;
        //int posZ = -1275;
        //String stringPattern = "1001011111001010111011100110111110101001011101011";
        // -720350949281663006
        //============================================================ END INPUT

        int offsetX = posX & 15;
        int offsetZ = posZ & 15;
        Integer[] pattern = stringPattern.chars().mapToObj(c -> c == '0' ? 0 : c == '1' ? 1 : 2).toArray(Integer[]::new);

        ReverserDevice device = new ReverserDevice();
        device.addCall(NextInt.withValue(16, offsetX));
        device.addCall(NextInt.withValue(16, offsetZ));
        device.addCall(NextInt.withValue(256, posY));
        device.addCall(NextInt.consume(2, 2)); //Skip size.

        for (Integer integer : pattern) {
            if (integer == 0) {
                device.addCall(NextInt.withValue(4, 0));
            } else if (integer == 1) {
                device.addCall(FilteredSkip.filter(r -> r.nextInt(4) != 0));
            } else {
                device.addCall(NextInt.consume(4, 1));
            }
        }

        Set<Long> decoratorSeeds = device.streamSeeds().sequential().limit(1).collect(Collectors.toSet());
        decoratorSeeds.forEach(s -> System.out.println("Found Dungeon seed: " + decoratorSeeds));

        System.out.format("Finished dungeon search and looking for world seeds.\n");

        for (long decoratorSeed : decoratorSeeds) {
            LCG failedDungeon = Rand.JAVA_LCG.combine(-5);

            for (int i = 0; i < 8; i++) {
                // they changed and added lake in the enum so 3 * 10000 + pos in the UNDERGROUND_STRUCTURES so 2 (yeah it changed)
                PopulationReversal.getWorldSeeds((decoratorSeed ^ Rand.JAVA_LCG.multiplier) - 30002L, posX & -16, posZ & -16).forEach(structureSeed -> {
                    System.out.format("Structure seed %d... \n", structureSeed);

                    for (long upperBits = 0; upperBits < (1L << 16); upperBits++) {
                        long worldSeed = (upperBits << 48) | structureSeed;
                        if (!RandomSeed.isRandomSeed(worldSeed)) continue;
                        System.out.format("\t With nextLong() equivalent %d.\n", worldSeed);
                    }
                });

                decoratorSeed = failedDungeon.nextSeed(decoratorSeed);
            }
        }
    }

}


