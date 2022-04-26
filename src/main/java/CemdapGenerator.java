import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CemdapGenerator {

    static final int zonesX = 10;
    static final int zonesY = zonesX;
    static int[][] residents = new int[zonesX][zonesY];
    static int[][] workplaces = new int[zonesX][zonesY];

    public void main() {
        String[] dbScript = generateCemdapData(2621);
    }

    private String[] generateCemdapData(int n) {
        List<List<Double>> persons = new ArrayList<>();
        List<List<Double>> households = new ArrayList<>();
        List<Map<String,Double>> zones = new ArrayList<>();
        LinkedList<List<Double>> zone2zone = new LinkedList<>();
        LinkedList<List<Double>> los = new LinkedList<>();

        for (int i = 0; i < zonesY * zonesX; i++) {
            for (int j = 0; j < zonesY * zonesX; j++) {
                zone2zone.add(new LinkedList<>());
                los.add(new LinkedList<>());
                zone2zone.getLast().add((double)i);
                los.getLast().add((double)i);
                zone2zone.getLast().add((double)j);
                los.getLast().add((double)j);
                los.getLast().add(i == j ? 1.0 : 0.0);
                if (Math.abs(i - j) == 1 && (i/zonesX == j/zonesX) || Math.abs((i / zonesX) - (j / zonesX)) == 1 && (i%zonesX == j%zonesX)) {
                    zone2zone.getLast().add(1.0);
                    los.getLast().add(1.0);
                } else {
                    zone2zone.getLast().add(0.0);
                    los.getLast().add(0.0);
                }
                double distance = 1.0 + Math.sqrt(Math.pow((i % zonesX) - (j % zonesX), 2) + Math.pow((i / zonesX) - (j / zonesX), 2));
                zone2zone.getLast().add(distance);
                los.getLast().add(distance);
                los.getLast().add(distance);
                los.getLast().add(3.1);
                los.getLast().add(1.0);
                los.getLast().add(distance * 1.5);
                los.getLast().add(distance / 3);
                los.getLast().add(8.0);
                los.getLast().add(distance * 3);
                los.getLast().add(5.0);
                los.getLast().add(29.0);
            }
        }

        for (int i = 0; i < zonesY * zonesX; i++) {
            zones.add(new LinkedHashMap<>());
            zones.get(i).put("ZONEID", (double) i);
            zones.get(i).put("SHOPDIST", 1.0);
            zones.get(i).put("REMPACC", 1.0);
            zones.get(i).put("RSEMPACC", 1.0);
            zones.get(i).put("TEMPACC", 1.0);
            zones.get(i).put("POPACC", 1.0);
            zones.get(i).put("DALCBD", 0.0);
            zones.get(i).put("FWCBD", 0.0);
            zones.get(i).put("MEDINC", 20.0);
            zones.get(i).put("NUMHH", 1000.0);
            zones.get(i).put("NUMPERS", 10000.0);
            zones.get(i).put("BEMP", 1.0);
            zones.get(i).put("REMP", 1.0);
            zones.get(i).put("SEMP", 1.0);
            zones.get(i).put("TOTEMP", 1.0);
            zones.get(i).put("PARKCOST", 3.0);
            zones.get(i).put("COUNTY", 1.0);
            zones.get(i).put("SPLLUSE", 1.0);
            zones.get(i).put("INTERNAL", 1.0);
        }

        OSMData osm = new OSMData(zonesX, zonesY);
        osm.init();

        Random r = new Random(125111);
        for (int i = 1; i < n; i++) {
            LinkedList<Double> p = new LinkedList<>();
            LinkedList<Double> h = new LinkedList<>();

            // Household ID
            double HHID = i;
            // Person ID
            double PerID = i;
            // Age of the person
            double Age;
            {
                int gauss = (int) Math.round(r.nextGaussian() * 15 + 40);
                if (gauss > 100) {
                    Age = 100;
                } else if (gauss < 18) {
                    Age = 18;
                } else {
                    Age = gauss;
                }
            }
            // adult is employed
            double aemp = (Age > 18 && r.nextDouble() > 0.5) ? 1.0 : 0.0;
            // adult or child is a student
            double stu = (aemp == 0 && r.nextDouble() > 0.8) ? 1.0 : 0.0;
            // Is the person licensed to drive
            double License = (Age > 18 && r.nextDouble() > 0.5) ? 1.0 : 0.0;
            // Work TSZ
            double WorkTSZ = (aemp == 1 ? osm.getRandomWorkZone() : -99);
            // School TSZ
            double SchTSZ;
            if (Age >= 6 && Age <= 18) {
                stu = 1;
            }
            if (stu == 1) {
                SchTSZ = r.nextInt(zonesX * zonesY);
            } else {
                SchTSZ = -99;
            }
            // Person is female
            double Female = Math.round(r.nextDouble());
            // Adult is a parent
            double parent = 0;
            // caucacian
            double cauc = 1;
            // african american
            double afamer = 0;
            // asian or pacific islander
            double asian = 0;
            // Gender of the person
            double Male = Female == 1 ? 0 : 1;
            // personal vehicle availability
            double pvehavbl = (License == 1 && r.nextDouble() < 0.78) ? 1 : 0;
            // high work flexibility
            double highflex = (aemp == 1 && r.nextDouble() > 0.7) ? 1 : 0;
            // pre school completed, child
            double presch = 0;
            // kindergarten to grade 4 completed, child
            double kgtog4 = 0;
            // grade 5 to grade 8 completed, child
            double g5tog8 = 0;
            // grade 9 or higher completed, child
            double g9orhigh = 0;
            // age of the person <= 5 years
            double lowage = (Age <= 5 ? 1 : 0);
            // lowage * one employed adult in household
            double loage1 = 0;
            // lowage * two employed adults in household
            double loage2 = 0;
            // some college completed, adult
            double somecol = (Age > 22 && r.nextDouble() > 0.7) ? 1 : 0;
            // associate or bachelors degree completed, adult
            double assobach = (somecol == 1 && r.nextDouble() > 0.8) ? 1 : 0;
            // masters or phd degree completed
            double mastphd = ((somecol==1) && !(assobach==1)) ? 1 : 0;
            // income in 1000s of dollars
            double income = 0;
            if (aemp == 1)
            {
                int gauss = (int) Math.round(r.nextGaussian() * 15 + 20);
                if (gauss < 0) {
                    income = 0;
                } else {
                    income = gauss;
                }
            }
            // income / HH income
            double incomef = income;
            // work duration between 0 and 20 hours a week
            double wdurlow = (aemp == 1 && Age < 22) ? 1 : 0;
            // work duration between 20 and 40 hours a week
            double wdurmed = (aemp == 1 && Age >= 22) ? 1 : 0;
            // employment type: construction and manufacturing
            double emptype1 = 0;
            // employment type: wholesale and transportation
            double emptype2 = 0;
            // employment type: personal, professional and financial services
            double emptype3 = 0;
            // employment type: public and military
            double emptype4 = 0;
            // employment type: retail and repair
            double emptype5 = 0;
            {
                double field = r.nextDouble();
                if (field > 0.8) { emptype1 = 1; }
                else if (field > 0.6) { emptype2 = 1; }
                else if (field > 0.4) { emptype3 = 1; }
                else if (field > 0.2) { emptype4 = 1; }
                else { emptype5 = 1; }
            }
            // female parent in a single parent or nuclear family houshold
            double mother = 0;
            // male parent in a single parent or nuclear family houshold
            double father = 0;
            // no school completed, child
            double nosch = 0;
            // child not a student
            double cnotstu = 0;
            // adult is unemployed
            double aunemp = (aemp == 0 && Age > 18) ? 1 : 0;
            // adult son or daughter in a single parent or nuclear family household
            double adchild = 0;
            // Person is 16 years of age or older
            double Adult = Age >= 16 ? 1 : 0;
            // hispanic
            double hisp = 0;
            // other race
            double othrace = 0;
            // grade 8 or lower completed, adult
            double g8orlow = Age > 18 ? 1 : 0;
            // grade 9 to grade 12 completed, adult
            double g9tog12 = Age > 18 ? 1 : 0;
            // high school completed, adult
            double highsch = (Age > 18 && somecol == 1) ? 1 : 0;
            // total weekly work duration (excluding weekend)
            double workhrs = 0;
            if (wdurlow == 1) { workhrs = 20; }
            else if (wdurmed == 1) { workhrs = 40; }
            // work duration greater than 40 hours a week
            double wdurhigh = 0;
            // medium work flexibility
            double medflex = (aemp == 1 && highflex == 0) ? 1 : 0;
            // low or no work flexibility
            double lowflex = 0;
            // employment type: other industries
            double emptype6 = 0;

            p.add(HHID);
            p.add(PerID);
            p.add(aemp);
            p.add(stu);
            p.add(License);
            p.add(WorkTSZ);
            p.add(SchTSZ);
            p.add(Female);
            p.add(Age);
            p.add(parent);
            p.add(cauc);
            p.add(afamer);
            p.add(asian);
            p.add(Male);
            p.add(pvehavbl);
            p.add(highflex);
            p.add(presch);
            p.add(kgtog4);
            p.add(g5tog8);
            p.add(g9orhigh);
            p.add(lowage);
            p.add(loage1);
            p.add(loage2);
            p.add(somecol);
            p.add(assobach);
            p.add(mastphd);
            p.add(income);
            p.add(incomef);
            p.add(wdurlow);
            p.add(wdurmed);
            p.add(emptype1);
            p.add(emptype2);
            p.add(emptype3);
            p.add(emptype4);
            p.add(emptype5);
            p.add(mother);
            p.add(father);
            p.add(nosch);
            p.add(cnotstu);
            p.add(aunemp);
            p.add(adchild);
            p.add(Adult);
            p.add(hisp);
            p.add(othrace);
            p.add(g8orlow);
            p.add(g9tog12);
            p.add(highsch);
            p.add(workhrs);
            p.add(wdurhigh);
            p.add(medflex);
            p.add(lowflex);
            p.add(emptype6);
            persons.add(p);

            //Number of adults
            double NADULT = 1;
            //Total number of HH vehicles, including motorcycles and RVs
            double NVEH = (pvehavbl == 1 ? 1 : 0);
            //Home TSZ location
            double HOMETSZ = osm.getRandomResidentialZone();
            //Number of children
            double NCHILD = 0;
            //household structure
            double HHSTRUCT = 1;
            //number of unemployed adults
            double NAUNEMP = (aemp == 1 ? 0 : 1);
            //household income in 1000s of dollars
            double HHINCOME = income;
            //household with no children
            double ZEROCH = 1;
            //Number of persons in household
            double NPERS = 1;
            //multiple numberof adults = 1 if nadult > 1
            double MULTADU = 0;
            //vehicles per licensed driver
            double VEHBYLIC = (License == 1 && pvehavbl == 1) ? 1 : 0;
            //single person household
            double SPERSON = 1;
            //single parent household
            double SPARENT = 0;
            //male-female couple household, no children
            double COUPLE = 0;
            //male-female couple household, with children
            double NUCLEAR = 0;
            //other household type
            double OHHTYPE = 0;
            //Number of licensed drivers
            double NUMLIC = License == 1 ? 1 : 0;
            //household with one child
            double ONECH = 0;
            //household with two or more children
            double TWOCH = 0;
            //Number of employed adults
            double NAEMP = aemp;
            //household with no employed adult
            double ZEROEMP = NAEMP == 0 ? 1 : 0;
            //household with one employed adult
            double ONEEMP = aemp;
            //household with two or more employed adults
            double TWOEMP = 0;
            //Number of adult students
            double NASTU = (stu == 1 && Adult == 1) ? 1 : 0;
            //Number of children not students
            double NCNOTSTU = 0;
            //Number of children who are students
            double NCSTU = 0;
            //own residential unit
            double OWNHOME = r.nextDouble() > 0.7 ? 1 : 0;
            //single family detached housing unit
            double SFDUNIT = 0;
            //single family attached housing unit
            double SFAUNIT = 0;
            //apartment type housing unit
            double APTUNIT = 1;
            //other type of housing unit
            double OTHUNIT = 0;

            h.add(HHID);
            h.add(NADULT);
            h.add(NVEH);
            h.add(HOMETSZ);
            h.add(NCHILD);
            h.add(HHSTRUCT);
            h.add(NAUNEMP);
            h.add(HHINCOME);
            h.add(ZEROCH);
            h.add(NPERS);
            h.add(MULTADU);
            h.add(VEHBYLIC);
            h.add(SPERSON);
            h.add(SPARENT);
            h.add(COUPLE);
            h.add(NUCLEAR);
            h.add(OHHTYPE);
            h.add(NUMLIC);
            h.add(ONECH);
            h.add(TWOCH);
            h.add(NAEMP);
            h.add(ZEROEMP);
            h.add(ONEEMP);
            h.add(TWOEMP);
            h.add(NASTU);
            h.add(NCNOTSTU);
            h.add(NCSTU);
            h.add(OWNHOME);
            h.add(SFDUNIT);
            h.add(SFAUNIT);
            h.add(APTUNIT);
            h.add(OTHUNIT);

            households.add(h);
        }

        List<String> outp = persons.stream().map(l -> "INSERT INTO public.persons(" +
                        "hid, pid, employed, studying, license, work_zon, stud_zon," +
                        " female, age, parent, caucasia, afriamer, asian, male, pvehavbl," +
                        " highflex, presch, kgtog4, g5tog8, g9orhigh, lowage, loage1, loage2," +
                        " somecol, assobach, mastphd, income, incomef, wdurlow, wdurmed," +
                        " emptype1, emptype2, emptype3, emptype4, emptype5, mother, father," +
                        " nosch, cnotstu, aunemp, adchild, adult, hisp, othrace, g8orlow," +
                        " g9tog12, highsch, workhrs, wdurhigh, medflex, lowflex, emptype6) " +
                        "VALUES (" +
                l.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")) + ");"
        ).collect(Collectors.toList());

        List<String> outh = households.stream().map(l -> "INSERT INTO public.households(" +
                        "hid, n_adults, n_autos, zone_id, kids, structure, naunemp, hhincome," +
                " zeroch, npers, multadu, vehbylic, sperson, sparent, couple, nuclear, ohhtype," +
                " numlic, onech, twoch, naemp, zeroemp, oneemp, twoemp, nastu, ncnotstu, ncstu," +
                " ownhome, sfdunit, sfaunit, aptunit, othunit) VALUES (" +
                l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) + ");"
        ).collect(Collectors.toList());

        List<String> outz = zones.stream().map(l -> "INSERT INTO public.zones(" +
                "zid, shopdist, rempacc, rsempacc, tempacc, popacc, dalcbd, fwcbd," +
                " medinc, numhh, numpers, bemp, remp, semp, totemp, parkcost, county," +
                " splluse, internal) " +
                "VALUES (" +
                l.values().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) + ");"
        ).collect(Collectors.toList());

        List<String> outz2z = zone2zone.stream().map(l -> "INSERT INTO public.zone2zone(" +
                "orig_zon, dest_zon, adjacent, distance) " +
                "VALUES (" +
                l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) + ");"
        ).collect(Collectors.toList());

        List<String> outLos = los.stream().map(l -> "INSERT INTO public.losoffpk(" +
                "orig_zon, dest_zon, same_zon, adjacent, distance, da_ivtt, da_ovtt," +
                " tr_avail, tr_ivtt, tr_ovtt, tr_cost, au_cost, sr_ivtt, sr_cost)" +
                "VALUES (" +
                l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) + ");"
        ).collect(Collectors.toList());

        write("original-input-data/plymouth/person.sql", outp);
        write("original-input-data/plymouth/household.sql", outh);
        write("original-input-data/plymouth/zone.sql", outz);
        write("original-input-data/plymouth/zone2zone.sql", outz2z);
        write("original-input-data/plymouth/los.sql", outLos);

        return null;
    }

    public void write(String filename, List<String> script) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (String line : script) {
                writer.write(line + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
