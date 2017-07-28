import com.dzd.cache.redis.connection.RedisConnectionFactory;
import com.dzd.cache.redis.manager.RedisClient;
import com.dzd.cache.redis.manager.RedisManager;
import com.dzd.db.mysql.MysqlOperator;
import com.dzd.utils.DateUtil;
import com.dzd.utils.StringUtil;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author WHL
 * @Date 2017-3-23.
 */
public class RedisClentTest {

    @Test
    public void testR() {
        //testR6();
    }
    public void testR6(){
        RedisClient redisClient = RedisManager.I.getRedisClient();
        redisClient.openResource();


        RedisConnectionFactory factory = RedisManager.I.getFactory();
        Jedis jedis = factory.getJedisConnection();
        String phones = "";
        long startTime = new Date().getTime();


        long keylen = jedis.hlen("sms_aisle_xr");
        List<String> taskIdList = null;
        String[] keys = getPhones();
        System.out.println("keylen="+keylen+", keys="+keys.length);
        for(int i=0;i<10;i++) {
            //redisClient.hget("key", "ss"+i );
            //String[] phones2= StringUtil.split(phones,",");
            //jedis.hget("key", "ss"+i );
            //jedis.hmget("sms_aisle_xr",keys);
            //List<String> taskIdList = jedis.hmget("sms_aisle_xr",keys);
            //String taskId = jedis.hget("sms_aisle_xr","aaaaaaaaaaaaaaaaaaaa");
            //jedis.hdel("sms_aisle_xr",keys);
            //String taskId = redisClient.hget("sms_aisle_xr","aaaaaaaaaaaaaaaaaaaa");
           // redisClient.hdel("sms_aisle_xr","aaaaaaaaaaaaaaaaaaaa");


            taskIdList = redisClient.hmget("sms_aisle_xr",keys);
            redisClient.hdel("sms_aisle_xr",keys);
        }

        System.out.println("taskIdList.size="+taskIdList.size());
        System.out.println("taskIdList.size="+taskIdList.get(0));
        jedis.close();
        redisClient.closeResource();

        System.out.println( "RUN TIME:"+( new Date().getTime()-startTime) );
    }
    public void testR5(){
        RedisClient redisClient = RedisManager.I.getRedisClient();
        RedisConnectionFactory factory = RedisManager.I.getFactory();
        Jedis jedis = factory.getJedisConnection();
        String phones = "";
        long startTime = new Date().getTime();
        for(int i=0;i<10000;i++) {
            //redisClient.hget("key", "ss"+i );
            String[] phones2= StringUtil.split(phones,",");
            jedis.hget("key", "ss"+i );
        }
        jedis.close();

        System.out.println( "RUN TIME:"+( new Date().getTime()-startTime) );
    }
    public void testR4(){
        RedisClient redisClient = RedisManager.I.getRedisClient();
        RedisConnectionFactory factory = RedisManager.I.getFactory();
        Jedis jedis = factory.getJedisConnection();
        long startTime = new Date().getTime();
        String phones="13684965192,15768775852,13684965192,13923443572,13923443572,15817392073,13684965192,13684965192,13928457665,13684965192,18566239192,18566239192,13684965192,13684965192,13684965192,13684965192,18566239192,15994768686,13684965192,15817392073,15817392073,15994768686,15817302073,15817392073,13684965192,13684965192,13684965192,13684965192,15817392073,15994768686,18928532922,13923443572,13428927894,15817392073,15817392073,13928457665,13527740510,13603043852,13622908468,18318142932,13502456092,13502456092,13652611878,13713456439,18898634506,13610538882,13729563760,13710267593,18218442005,13682382886,15820347752,13623092160,13450040241,13760372439,13411216178,13692023754,13790929953,13692845485,13726171419,15918439828,13802864445,17820113157,15118156224,13510249397,15913861202,15913861202,13828663375,13539083329,15994768686,13824007972,13724078983,13825038260,18344109380,15220160064,13684965192,13539083329,15994768686,13417609588,13719081921,13829951995,13924483432,13924483432,13570098473,13714843682,13528027565,18816845402,15976908475,15014431660,13501566467,13413691613,13824230310,13528803883,18318302218,13750207238,18420179580,15768874152,15976987120,15876454013,13431310700,13422900923,13580537998,15767115557,13646799908,13530677578,13640076163,15728309708,13544375886,13829037974,13695100000,13686743092,18814384713,13653011820,13750432482,18316887823,18825473723,15913861202,15889932702,18825057321,15915582202,15915981202,15802026607,15015999808,13726852523,18898360850,13480509172,13679824716,13539645582,13695175964,18218529391,13670555602,15913927875,13421102158,15767318060,13542316162,15816530537,13533100894,15802050789,13424260558,18476668137,13553677420,15815070463,15766363566,18718546694,13699791264,13751347585,13790822042,13432437409,15113516260,13510863865,18813920887,13542529391,18200890803,15920065309,15920144886,18826248509,13727678989,15018110316,18816793817,15920144886,13727835542,13527046091,13510863865,18218166981,15820719168,18826050515,17875627948,13414665965,15814393005,18320965530,13510930278,15915447449,13516628227,18813920887,13809738703,13512799467,13929739837,13437734168,13553232931,15900168060,13692347531,18316311786,15818933440,15015350507,15975920800,15815337659,13751908919,13724744637,13751908919,15012977130,14718444085,13798391048,18475535852,13725303453,18219417757,18219232529,18320657772,18312715353,13640432933,15019595194,13640432933,18718603755,18825473723,13728032791,18824100624,13678975557,18320559588,18320559588,13714395652,13420772680,18813746579,15899970341,18718991561,18718991561,15975233001,13760754696,13413352916,13428118884,18898717393,17820113157,15119289018,15119386873,15975233001,13680420609,13729001045,15876918863,15899969788,15914932353,15914932353,13660976787,15820283751,18211369940,18820562584,14718444085,13422918468,15820918611,15018886318,15815094720,13726111982,13600234422,13672588167,18819496710,15814042852,15089306693,18719070089,13410569990,13692534496,13610373114,15018815013,13729332341,13553324086,13531166110,13531949039,15820104905,18718603509,13824662287,13433354360,13428118884,18814387732,13531616826,15024275571,15813379817,15813216498,13660974777,15728395807,15813666595,15218294294,18364628562,15018259972,13543115804,13553236400,15976270797,13672720754,13539927122,15914896552,13414058906,13760659470,15986202378,15976519123,18825072118,13428167513,13711257293,13417639328,13760562497,15920937366,15817214704,18344505651,13763051585,13432919296,13652865495,13612702819,18819482473,15819283499,13501592852,18200677588,18826109146,18898631012,15018491506,15813963494,13623074957,13433971332,15218784862,13751851727,13631968969,13924773936,13417292789,15816285703,18824699619,13501592852,18814312193,18218845766,18218845766,13794121418,15813963494,15218373179,15767823791,18302030684,13543333120,18718207672,13480597924,13542893545,15119736743,13826448732,15218341249,15099840219,13411957027,15914453432,15989803395,13929504734,15889843375,15975139521,15820347752,13543115804,13172031082,13662701897,15766694916,13528850550,13682875236,18316335876,13414920793,13612246304,13822085108,15918996578,15918996578,13750061659,18813430310,15920710646,13723471671,13632015426,18475535852,13717080880,15119431810,13684953516,13682853306,18218260878,15889632903,13432994093,13692981259,13928922611,13631330311,15107552382,13580425045,13729377522,17876676064,15013943022,13642442351,18300082083,17875780793,13539891031,13536868926,15707502420,13143684226,15219991327,15875963390,13825959575,13717216779,14715585583,13790907381,13826365832,13435918624,13590035250,13433960846,13433960846,18320164584,13724744637,15818826740,17876202977,13610210093,13580651357,13413029450,13727766479,18718356515,15219326710,13434960546,15113823402,15767762210,13570902236,15976266973,13622974117,13826807110,18806694718,13642602850,13750563639,15989151052,13592922449,13682894041,15889843375,13750527894,13827605891,13570416467,18819143002,15119237932,15920122602,18282995469,18282995469,13536324605,13430233429,13822652770,15915657011,13421501597,18824791169,15889825289,15889825289,15889825289,18814423254,13480387396,13534935032,13729554290,15992318064,13760831332,15918655953,13719492065,15218446660,15202055876,13660435955,18122718520,13560564624,18122718520,15766383521,13662843409,18318826989,18718639065,15220392042,15220392042,15914089953,13822595815,13729514140,18898717393,13760778957,17188807131,13726537506,15816101311,15916955887,15813953184,13610538882,17875158844,13432384141,18475184490,13640789982,13719563583,18306683744,13433326383,15768524439,15800028871,13924546732,13450272174,13437676250,13680188833,13697428141,15989539408,13592714094,13413952077,13539680375,13592714094,13726599051,15767619347,15107552382,18344329676,13560095166,15976202223,15219326710,15724146061,13539920374,14778188084,15766392151,15767399521,15017289521,15017289521,15017289521,15017289521,15219326710,15976202223,15219326710,18816795861,13751745585,15216927327,15219326409,13610343420,15915897901,15216927327,13609055535,13609055535,13927494106,13751890603,13631825739,13751890603,15089531998,15915790021,15119847522,13798292993,13433319758,13924777005,13798221941,15815686420,13590075940,18319270136,15919869685,13642525882,15815440098,13609055535,13763318883,13798221941,13413013114,13826206218,15814606654,15113312944,13726963618,13709697672,15813043492,15820334957,13692583507,15989754041,13538803117,15915767893,18824386409,18826078787,13642355206,15768193866,15986901508,15089581500,18826109146,18211542237,13612881757,15889569819,18316694189,13431836210,15992598293,15992598293,13411985363,13660268737,13763052601,13539613574,15976460010,15914181991,15820394554,13750278348,15986901508,15119668653,15119668653,13929465191,13543877341,13929465191,13416143569,18402029793,13724223676,15107552382,15113506951,15218126489,15089895165,13542454394,18819225938,14718438370,18476940703,13650986340,13556862048,13642229989,18718817712,15119448038,13631963643,15018233895,15989826560,13924483432,13924483432,15018233895,13559727357,15767960151,18819372525,15807598990,18475535852,15089280103,15013176784,18344592243,18825069795,15816697494,17876570968,14718211345,17876570968,17876570968,17876570968,13570324099,18475541861,13536998816,18475541861,13710030929,15976567682,18823157224,13650093795,15975655682,15992313720,13542326094,13412952078,13415066611,13723640003,15218184975,15220214131,13924772170,13684918895,15815464097,13822548643,13827170767,13728792379,13433806424,18219123170,18319580384,18824065165,15917063844,18319091479,13662342137,13662342137,13662342137,13662342137,13662342137,13662342137,15218295225,13790909670,13790909670,13790909670,13790909670,13714527602,13809784814,18820230141,13411750049,13415121428,18312692128,17807630255,15919211133,13620291245,15814740673,15814740673,15814740673,15113114895,15819390735,15018029467,15989826549,13553286430,13530044801,13530808989,13902588005,13592819372,13924787250,18219498744,18318161439,13536813299,18318104128,15218303384,13929714183,13527757247,13527757247,15919306069,18826229503,13613068798,13434251233,13669571422,13929352991,13692735964,15014708812,15014708812,18316887823,15014708812,15876776723,18319742037,15986316033,15219843902,13535181458,13539834005,18312691083,18813295587,15919319866,18319157054,15900168060,13719300194,13602881245,15218393634,15889458753,13602790715,13922390559,13712837936,15907512685,15802077206,13413691613,18219070597,18320391921,18320391921,18819308627,13431269561,15913157478,13794121418,13570487276,18677167879,18813583605,13828942677,15219248845,13422909810,18476595279,15217634607,15816503913,18219440565,15889146097,17817538787,17817538787,15986781161,13480574508,13533225955,13450313264,13410895924,13715739727,13531064602,15992677841,18218915006,13729300893,15218888089,17817538787,15820193245,15018773454,13713733120,13610024828,13610024828,18319356812,13414013675,13411074348,13631768150,15018510568,18819868666,13509975239,17820050683,14778559657,18316872327,15218837242,13414665965,15118139380,15219843902,13509975239,15916410672,15916410672,15916410672,13535908376,15706623575,13535908376,18318254900,13423653944,15976554342,15089797579,15089797579,13535272097,15089797579,13480597924,15914249104,15119021548,13660248425,18319786244,15219326710,17820113157,15813952280,13631390583,13416197407,15089618792,15914850342,13710323080,13809639561,13600012842,13710323080,18816833599,13610394845,13610394845,13432892978,17875458186,14718396683,13410073374,13923984864,13527815768,18707677875,18820040497,13553236400,13622929510,15976717197,15816765767,13725124564,15218305622,18475069893,13553392110,18475888138,15815204120,17876635461,13662453851,18898786885,15820917823,13690870092,15813780445,17876587934,13763291312,18312015260,15876924907,13763042305,15989240282,13509973503,13631973332,13711091787,18319901804,13411072826,15118331155,15118331155,18718434809,15016590772,13710032656,13422841260,15813666595,15707618933,13600093317,13430439347,18475782270,13643066897,15915017507,13415792050,17875780793,13620430270,15113988225,13423503050,15768596012,15899882593,13662453851,18826476578,18320688584,15207520230,17802074131,18206642155,13727712747,18316882878,18218022029,15816370465,13902513297,15099772328,15820909959,15019959595,18320134462,15915768852,18218022029,15976791785,15119674698,13729244509,15767137990,13534538018,15989732751,13435272286,13434217526,17806695660,13437457292,15014302137,15986311923,13413147401,13413147401,15089852627,15089852627,13719196459,13422841260,18202038057,13682931615,13682931615,13682931615,13682931615,15089871144,13113083999,13680038626,15089852627,18318613045,18814118089,15220576167,13686492942,13686492942,13652560953,15986579868,13713212995,15706692310,15107620526,13510559395,13450180361,15017173697,13650647673,15016337702,17875734027,13538667316,13829048980,14718364663,13433344246,15775071472,13570940243,13590010261,13425596840,13425596840,15820725413,18826229694,15917104152,15917104152,13822027652,13710143458,15113511132,13713034715,13631726706,15815378716,15220018041,13710081440,13432747620,13790779257,13430244787,15914853654,13829382589,18219393698,18718639065,15012977130,15218201756,13790797198,13434901258,13590273186,18814499464,15989975872,15217836509,17875860932,18826672532,15220716752,13433971332,13924223189,15919531450,18898624030,13542436900,15815820596,13729878052,15819379944,13672570634,14715591972,15819379944,18718639065,15820347752,13610211282,13727370532,13430876796,18344501326,15913508743,13570551656,18420424494,13825690173,13825690173,15819379944,13825690173,15707511356,15800283549,18719102348,15813874834,15766795527,13711678621,13711678621,18318105366,13794541140,13417960119,17875538060,18825068589,13420515516,13726994940,15800298272,15807648204,15818970073,13822868839,15018591676,13825054339,15218314990,13682313546,18813263225,15819379944,13678995479,15218009324,15820676571,13927222539,18807593837,13532935759,15920345750,18344406160,15218314990,15816499523,15999503004,17817272833,13632376091,13632376091";
        phones=phones+phones;
        for(int i=0;i<1000;i++) {
            redisClient.hset("key", "ss"+i,phones);
            String[] phones2= StringUtil.split(phones,",");
            //jedis.hset("key", "ss"+i,phones);
        }


//        String phoneStr = "1234343,2,3,3";
//        Map<String,String> a = new HashMap<String,String>();
//        a.put("String","string");
//        for( int n=0;n<1000000; n++){
//            String[] phoneStrInfo = StringUtil.split( phoneStr,",");
//            a.equals( phoneStrInfo[0]);
//
//        }

        jedis.close();

        System.out.println( "RUN TIME:"+( new Date().getTime()-startTime) );
    }


    public void testR2(){
        System.out.println("-");
        RedisClient redisClient = RedisManager.I.getRedisClient();

        redisClient.set("a","b" );
        System.out.println( redisClient.get("a"));



        try {
            String sql = " SELECT * FROM sms_blacklist  ";
            List<String> blacklists = MysqlOperator.I.query(sql, new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getString("phone");
                }
            });

            for (String k : blacklists) {

                    System.out.println("k=" + k);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String[] getPhones(){
        String phones="13684965192,15768775852,13684965192,13923443572,13923443572,15817392073,13684965192,13684965192,13928457665,13684965192,18566239192,18566239192,13684965192,13684965192,13684965192,13684965192,18566239192,15994768686,13684965192,15817392073,15817392073,15994768686,15817302073,15817392073,13684965192,13684965192,13684965192,13684965192,15817392073,15994768686,18928532922,13923443572,13428927894,15817392073,15817392073,13928457665,13527740510,13603043852,13622908468,18318142932,13502456092,13502456092,13652611878,13713456439,18898634506,13610538882,13729563760,13710267593,18218442005,13682382886,15820347752,13623092160,13450040241,13760372439,13411216178,13692023754,13790929953,13692845485,13726171419,15918439828,13802864445,17820113157,15118156224,13510249397,15913861202,15913861202,13828663375,13539083329,15994768686,13824007972,13724078983,13825038260,18344109380,15220160064,13684965192,13539083329,15994768686,13417609588,13719081921,13829951995,13924483432,13924483432,13570098473,13714843682,13528027565,18816845402,15976908475,15014431660,13501566467,13413691613,13824230310,13528803883,18318302218,13750207238,18420179580,15768874152,15976987120,15876454013,13431310700,13422900923,13580537998,15767115557,13646799908,13530677578,13640076163,15728309708,13544375886,13829037974,13695100000,13686743092,18814384713,13653011820,13750432482,18316887823,18825473723,15913861202,15889932702,18825057321,15915582202,15915981202,15802026607,15015999808,13726852523,18898360850,13480509172,13679824716,13539645582,13695175964,18218529391,13670555602,15913927875,13421102158,15767318060,13542316162,15816530537,13533100894,15802050789,13424260558,18476668137,13553677420,15815070463,15766363566,18718546694,13699791264,13751347585,13790822042,13432437409,15113516260,13510863865,18813920887,13542529391,18200890803,15920065309,15920144886,18826248509,13727678989,15018110316,18816793817,15920144886,13727835542,13527046091,13510863865,18218166981,15820719168,18826050515,17875627948,13414665965,15814393005,18320965530,13510930278,15915447449,13516628227,18813920887,13809738703,13512799467,13929739837,13437734168,13553232931,15900168060,13692347531,18316311786,15818933440,15015350507,15975920800,15815337659,13751908919,13724744637,13751908919,15012977130,14718444085,13798391048,18475535852,13725303453,18219417757,18219232529,18320657772,18312715353,13640432933,15019595194,13640432933,18718603755,18825473723,13728032791,18824100624,13678975557,18320559588,18320559588,13714395652,13420772680,18813746579,15899970341,18718991561,18718991561,15975233001,13760754696,13413352916,13428118884,18898717393,17820113157,15119289018,15119386873,15975233001,13680420609,13729001045,15876918863,15899969788,15914932353,15914932353,13660976787,15820283751,18211369940,18820562584,14718444085,13422918468,15820918611,15018886318,15815094720,13726111982,13600234422,13672588167,18819496710,15814042852,15089306693,18719070089,13410569990,13692534496,13610373114,15018815013,13729332341,13553324086,13531166110,13531949039,15820104905,18718603509,13824662287,13433354360,13428118884,18814387732,13531616826,15024275571,15813379817,15813216498,13660974777,15728395807,15813666595,15218294294,18364628562,15018259972,13543115804,13553236400,15976270797,13672720754,13539927122,15914896552,13414058906,13760659470,15986202378,15976519123,18825072118,13428167513,13711257293,13417639328,13760562497,15920937366,15817214704,18344505651,13763051585,13432919296,13652865495,13612702819,18819482473,15819283499,13501592852,18200677588,18826109146,18898631012,15018491506,15813963494,13623074957,13433971332,15218784862,13751851727,13631968969,13924773936,13417292789,15816285703,18824699619,13501592852,18814312193,18218845766,18218845766,13794121418,15813963494,15218373179,15767823791,18302030684,13543333120,18718207672,13480597924,13542893545,15119736743,13826448732,15218341249,15099840219,13411957027,15914453432,15989803395,13929504734,15889843375,15975139521,15820347752,13543115804,13172031082,13662701897,15766694916,13528850550,13682875236,18316335876,13414920793,13612246304,13822085108,15918996578,15918996578,13750061659,18813430310,15920710646,13723471671,13632015426,18475535852,13717080880,15119431810,13684953516,13682853306,18218260878,15889632903,13432994093,13692981259,13928922611,13631330311,15107552382,13580425045,13729377522,17876676064,15013943022,13642442351,18300082083,17875780793,13539891031,13536868926,15707502420,13143684226,15219991327,15875963390,13825959575,13717216779,14715585583,13790907381,13826365832,13435918624,13590035250,13433960846,13433960846,18320164584,13724744637,15818826740,17876202977,13610210093,13580651357,13413029450,13727766479,18718356515,15219326710,13434960546,15113823402,15767762210,13570902236,15976266973,13622974117,13826807110,18806694718,13642602850,13750563639,15989151052,13592922449,13682894041,15889843375,13750527894,13827605891,13570416467,18819143002,15119237932,15920122602,18282995469,18282995469,13536324605,13430233429,13822652770,15915657011,13421501597,18824791169,15889825289,15889825289,15889825289,18814423254,13480387396,13534935032,13729554290,15992318064,13760831332,15918655953,13719492065,15218446660,15202055876,13660435955,18122718520,13560564624,18122718520,15766383521,13662843409,18318826989,18718639065,15220392042,15220392042,15914089953,13822595815,13729514140,18898717393,13760778957,17188807131,13726537506,15816101311,15916955887,15813953184,13610538882,17875158844,13432384141,18475184490,13640789982,13719563583,18306683744,13433326383,15768524439,15800028871,13924546732,13450272174,13437676250,13680188833,13697428141,15989539408,13592714094,13413952077,13539680375,13592714094,13726599051,15767619347,15107552382,18344329676,13560095166,15976202223,15219326710,15724146061,13539920374,14778188084,15766392151,15767399521,15017289521,15017289521,15017289521,15017289521,15219326710,15976202223,15219326710,18816795861,13751745585,15216927327,15219326409,13610343420,15915897901,15216927327,13609055535,13609055535,13927494106,13751890603,13631825739,13751890603,15089531998,15915790021,15119847522,13798292993,13433319758,13924777005,13798221941,15815686420,13590075940,18319270136,15919869685,13642525882,15815440098,13609055535,13763318883,13798221941,13413013114,13826206218,15814606654,15113312944,13726963618,13709697672,15813043492,15820334957,13692583507,15989754041,13538803117,15915767893,18824386409,18826078787,13642355206,15768193866,15986901508,15089581500,18826109146,18211542237,13612881757,15889569819,18316694189,13431836210,15992598293,15992598293,13411985363,13660268737,13763052601,13539613574,15976460010,15914181991,15820394554,13750278348,15986901508,15119668653,15119668653,13929465191,13543877341,13929465191,13416143569,18402029793,13724223676,15107552382,15113506951,15218126489,15089895165,13542454394,18819225938,14718438370,18476940703,13650986340,13556862048,13642229989,18718817712,15119448038,13631963643,15018233895,15989826560,13924483432,13924483432,15018233895,13559727357,15767960151,18819372525,15807598990,18475535852,15089280103,15013176784,18344592243,18825069795,15816697494,17876570968,14718211345,17876570968,17876570968,17876570968,13570324099,18475541861,13536998816,18475541861,13710030929,15976567682,18823157224,13650093795,15975655682,15992313720,13542326094,13412952078,13415066611,13723640003,15218184975,15220214131,13924772170,13684918895,15815464097,13822548643,13827170767,13728792379,13433806424,18219123170,18319580384,18824065165,15917063844,18319091479,13662342137,13662342137,13662342137,13662342137,13662342137,13662342137,15218295225,13790909670,13790909670,13790909670,13790909670,13714527602,13809784814,18820230141,13411750049,13415121428,18312692128,17807630255,15919211133,13620291245,15814740673,15814740673,15814740673,15113114895,15819390735,15018029467,15989826549,13553286430,13530044801,13530808989,13902588005,13592819372,13924787250,18219498744,18318161439,13536813299,18318104128,15218303384,13929714183,13527757247,13527757247,15919306069,18826229503,13613068798,13434251233,13669571422,13929352991,13692735964,15014708812,15014708812,18316887823,15014708812,15876776723,18319742037,15986316033,15219843902,13535181458,13539834005,18312691083,18813295587,15919319866,18319157054,15900168060,13719300194,13602881245,15218393634,15889458753,13602790715,13922390559,13712837936,15907512685,15802077206,13413691613,18219070597,18320391921,18320391921,18819308627,13431269561,15913157478,13794121418,13570487276,18677167879,18813583605,13828942677,15219248845,13422909810,18476595279,15217634607,15816503913,18219440565,15889146097,17817538787,17817538787,15986781161,13480574508,13533225955,13450313264,13410895924,13715739727,13531064602,15992677841,18218915006,13729300893,15218888089,17817538787,15820193245,15018773454,13713733120,13610024828,13610024828,18319356812,13414013675,13411074348,13631768150,15018510568,18819868666,13509975239,17820050683,14778559657,18316872327,15218837242,13414665965,15118139380,15219843902,13509975239,15916410672,15916410672,15916410672,13535908376,15706623575,13535908376,18318254900,13423653944,15976554342,15089797579,15089797579,13535272097,15089797579,13480597924,15914249104,15119021548,13660248425,18319786244,15219326710,17820113157,15813952280,13631390583,13416197407,15089618792,15914850342,13710323080,13809639561,13600012842,13710323080,18816833599,13610394845,13610394845,13432892978,17875458186,14718396683,13410073374,13923984864,13527815768,18707677875,18820040497,13553236400,13622929510,15976717197,15816765767,13725124564,15218305622,18475069893,13553392110,18475888138,15815204120,17876635461,13662453851,18898786885,15820917823,13690870092,15813780445,17876587934,13763291312,18312015260,15876924907,13763042305,15989240282,13509973503,13631973332,13711091787,18319901804,13411072826,15118331155,15118331155,18718434809,15016590772,13710032656,13422841260,15813666595,15707618933,13600093317,13430439347,18475782270,13643066897,15915017507,13415792050,17875780793,13620430270,15113988225,13423503050,15768596012,15899882593,13662453851,18826476578,18320688584,15207520230,17802074131,18206642155,13727712747,18316882878,18218022029,15816370465,13902513297,15099772328,15820909959,15019959595,18320134462,15915768852,18218022029,15976791785,15119674698,13729244509,15767137990,13534538018,15989732751,13435272286,13434217526,17806695660,13437457292,15014302137,15986311923,13413147401,13413147401,15089852627,15089852627,13719196459,13422841260,18202038057,13682931615,13682931615,13682931615,13682931615,15089871144,13113083999,13680038626,15089852627,18318613045,18814118089,15220576167,13686492942,13686492942,13652560953,15986579868,13713212995,15706692310,15107620526,13510559395,13450180361,15017173697,13650647673,15016337702,17875734027,13538667316,13829048980,14718364663,13433344246,15775071472,13570940243,13590010261,13425596840,13425596840,15820725413,18826229694,15917104152,15917104152,13822027652,13710143458,15113511132,13713034715,13631726706,15815378716,15220018041,13710081440,13432747620,13790779257,13430244787,15914853654,13829382589,18219393698,18718639065,15012977130,15218201756,13790797198,13434901258,13590273186,18814499464,15989975872,15217836509,17875860932,18826672532,15220716752,13433971332,13924223189,15919531450,18898624030,13542436900,15815820596,13729878052,15819379944,13672570634,14715591972,15819379944,18718639065,15820347752,13610211282,13727370532,13430876796,18344501326,15913508743,13570551656,18420424494,13825690173,13825690173,15819379944,13825690173,15707511356,15800283549,18719102348,15813874834,15766795527,13711678621,13711678621,18318105366,13794541140,13417960119,17875538060,18825068589,13420515516,13726994940,15800298272,15807648204,15818970073,13822868839,15018591676,13825054339,15218314990,13682313546,18813263225,15819379944,13678995479,15218009324,15820676571,13927222539,18807593837,13532935759,15920345750,18344406160,15218314990,15816499523,15999503004,17817272833,13632376091,13632376091";
        return StringUtil.split(phones,",");
    }
}