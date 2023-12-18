/*
 * Copyright (C) 2021 by Don F. Becker <don@donfbecker.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.donfbecker.telemetryreceiver;

public class LowPassFilter {
    private final int SAMPLE_SIZE = 480;
    //private float[] coefficients = {0.0006920,0.0009442,0.0013667,0.0020309,0.0030003,0.0043263,0.0060426,0.0081621,0.0106741,0.0135433,0.0167100,0.0200920,0.0235883,0.0270835,0.0304538,0.0335729,0.0363196,0.0385836,0.0402719,0.0413143,0.0416667,0.0413143,0.0402719,0.0385836,0.0363196,0.0335729,0.0304538,0.0270835,0.0235883,0.0200920,0.0167100,0.0135433,0.0106741,0.0081621,0.0060426,0.0043263,0.0030003,0.0020309,0.0013667,0.0009442};

    //Filter Order: 480 Sampling Frequency (Hz): 48000.000000 Cut-Off Frequency Lo (Hz): 1000.000000 Cut-Off Frequency Hi (Hz): 1000.000000
    //private float[] coefficients = {0.20000000,-0.0000151,-0.0000302,-0.0000449,-0.0000591,-0.0000725,-0.0000850,-0.0000963,-0.0001063,-0.0001147,-0.0001215,-0.0001263,-0.0001292,-0.0001301,-0.0001287,-0.0001251,-0.0001193,-0.0001112,-0.0001010,-0.0000886,-0.0000742,-0.0000579,-0.0000400,-0.0000206,0.0000000,0.0000215,0.0000436,0.0000659,0.0000880,0.0001097,0.0001304,0.0001499,0.0001676,0.0001831,0.0001962,0.0002063,0.0002133,0.0002168,0.0002166,0.0002124,0.0002042,0.0001918,0.0001753,0.0001548,0.0001304,0.0001024,0.0000710,0.0000367,0.0000000,-0.0000387,-0.0000786,-0.0001192,-0.0001597,-0.0001995,-0.0002376,-0.0002733,-0.0003060,-0.0003346,-0.0003587,-0.0003774,-0.0003902,-0.0003965,-0.0003960,-0.0003881,-0.0003728,-0.0003499,-0.0003195,-0.0002818,-0.0002370,-0.0001858,-0.0001287,-0.0000665,0.0000000,0.0000697,0.0001414,0.0002140,0.0002861,0.0003565,0.0004237,0.0004863,0.0005431,0.0005926,0.0006337,0.0006652,0.0006860,0.0006954,0.0006927,0.0006773,0.0006489,0.0006075,0.0005534,0.0004868,0.0004085,0.0003194,0.0002207,0.0001137,0.0000000,-0.0001186,-0.0002401,-0.0003625,-0.0004835,-0.0006010,-0.0007126,-0.0008161,-0.0009092,-0.0009899,-0.0010561,-0.0011061,-0.0011383,-0.0011515,-0.0011445,-0.0011166,-0.0010677,-0.0009976,-0.0009069,-0.0007962,-0.0006669,-0.0005205,-0.0003589,-0.0001846,0.0000000,0.0001918,0.0003878,0.0005845,0.0007784,0.0009661,0.0011438,0.0013079,0.0014551,0.0015820,0.0016856,0.0017632,0.0018123,0.0018310,0.0018177,0.0017716,0.0016921,0.0015795,0.0014344,0.0012583,0.0010530,0.0008212,0.0005659,0.0002908,0.0000000,-0.0003019,-0.0006099,-0.0009188,-0.0012232,-0.0015175,-0.0017961,-0.0020534,-0.0022841,-0.0024831,-0.0026457,-0.0027675,-0.0028448,-0.0028747,-0.0028546,-0.0027830,-0.0026593,-0.0024834,-0.0022566,-0.0019807,-0.0016588,-0.0012946,-0.0008929,-0.0004593,0.0000000,0.0004779,0.0009667,0.0014584,0.0019444,0.0024161,0.0028645,0.0032809,0.0036566,0.0039835,0.0042535,0.0044598,0.0045958,0.0046562,0.0046365,0.0045335,0.0043454,0.0040713,0.0037122,0.0032703,0.0027493,0.0021544,0.0014923,0.0007710,0.0000000,-0.0008100,-0.0016472,-0.0024987,-0.0033508,-0.0041890,-0.0049985,-0.0057640,-0.0064701,-0.0071016,-0.0076435,-0.0080815,-0.0084020,-0.0085923,-0.0086409,-0.0085378,-0.0082745,-0.0078442,-0.0072419,-0.0064647,-0.0055118,-0.0043843,-0.0030857,-0.0016216,0.0000000,0.0017693,0.0036744,0.0057011,0.0078340,0.0100554,0.0123467,0.0146876,0.0170571,0.0194334,0.0217938,0.0241158,0.0263767,0.0285542,0.0306263,0.0325722,0.0343719,0.0360069,0.0374604,0.0387171,0.0397638,0.0405897,0.0411859,0.0415462,0.0416667,0.0415462,0.0411859,0.0405897,0.0397638,0.0387171,0.0374604,0.0360069,0.0343719,0.0325722,0.0306263,0.0285542,0.0263767,0.0241158,0.0217938,0.0194334,0.0170571,0.0146876,0.0123467,0.0100554,0.0078340,0.0057011,0.0036744,0.0017693,0.0000000,-0.0016216,-0.0030857,-0.0043843,-0.0055118,-0.0064647,-0.0072419,-0.0078442,-0.0082745,-0.0085378,-0.0086409,-0.0085923,-0.0084020,-0.0080815,-0.0076435,-0.0071016,-0.0064701,-0.0057640,-0.0049985,-0.0041890,-0.0033508,-0.0024987,-0.0016472,-0.0008100,0.0000000,0.0007710,0.0014923,0.0021544,0.0027493,0.0032703,0.0037122,0.0040713,0.0043454,0.0045335,0.0046365,0.0046562,0.0045958,0.0044598,0.0042535,0.0039835,0.0036566,0.0032809,0.0028645,0.0024161,0.0019444,0.0014584,0.0009667,0.0004779,0.0000000,-0.0004593,-0.0008929,-0.0012946,-0.0016588,-0.0019807,-0.0022566,-0.0024834,-0.0026593,-0.0027830,-0.0028546,-0.0028747,-0.0028448,-0.0027675,-0.0026457,-0.0024831,-0.0022841,-0.0020534,-0.0017961,-0.0015175,-0.0012232,-0.0009188,-0.0006099,-0.0003019,0.0000000,0.0002908,0.0005659,0.0008212,0.0010530,0.0012583,0.0014344,0.0015795,0.0016921,0.0017716,0.0018177,0.0018310,0.0018123,0.0017632,0.0016856,0.0015820,0.0014551,0.0013079,0.0011438,0.0009661,0.0007784,0.0005845,0.0003878,0.0001918,0.0000000,-0.0001846,-0.0003589,-0.0005205,-0.0006669,-0.0007962,-0.0009069,-0.0009976,-0.0010677,-0.0011166,-0.0011445,-0.0011515,-0.0011383,-0.0011061,-0.0010561,-0.0009899,-0.0009092,-0.0008161,-0.0007126,-0.0006010,-0.0004835,-0.0003625,-0.0002401,-0.0001186,0.0000000,0.0001137,0.0002207,0.0003194,0.0004085,0.0004868,0.0005534,0.0006075,0.0006489,0.0006773,0.0006927,0.0006954,0.0006860,0.0006652,0.0006337,0.0005926,0.0005431,0.0004863,0.0004237,0.0003565,0.0002861,0.0002140,0.0001414,0.0000697,0.0000000,-0.0000665,-0.0001287,-0.0001858,-0.0002370,-0.0002818,-0.0003195,-0.0003499,-0.0003728,-0.0003881,-0.0003960,-0.0003965,-0.0003902,-0.0003774,-0.0003587,-0.0003346,-0.0003060,-0.0002733,-0.0002376,-0.0001995,-0.0001597,-0.0001192,-0.0000786,-0.0000387,0.0000000,0.0000367,0.0000710,0.0001024,0.0001304,0.0001548,0.0001753,0.0001918,0.0002042,0.0002124,0.0002166,0.0002168,0.0002133,0.0002063,0.0001962,0.0001831,0.0001676,0.0001499,0.0001304,0.0001097,0.0000880,0.0000659,0.0000436,0.0000215,0.0000000,-0.0000206,-0.0000400,-0.0000579,-0.0000742,-0.0000886,-0.0001010,-0.0001112,-0.0001193,-0.0001251,-0.0001287,-0.0001301,-0.0001292,-0.0001263,-0.0001215,-0.0001147,-0.0001063,-0.0000963,-0.0000850,-0.0000725,-0.0000591,-0.0000449,-0.0000302,-0.0000151};


    //Filter Order: 480 Sampling Frequency (Hz): 48000.000000 Cut-Off Frequency Lo (Hz): 1000.000000 Cut-Off Frequency Hi (Hz): 20.000000
    //private float[] coefficients = {0.0000000,-0.0008840,-0.0017910,-0.0027069,-0.0036172,-0.0045064,-0.0053590,-0.0061593,-0.0068916,-0.0075405,-0.0080912,-0.0085294,-0.0088419,-0.0090168,-0.0090431,-0.0089115,-0.0086145,-0.0081462,-0.0075026,-0.0066819,-0.0056841,-0.0045116,-0.0031686,-0.0016619,0.0000000,0.0018064,0.0037448,0.0058006,0.0079577,0.0101987,0.0125044,0.0148548,0.0172290,0.0196053,0.0219617,0.0242759,0.0265258,0.0286897,0.0307464,0.0326756,0.0344581,0.0360760,0.0375132,0.0387550,0.0397887,0.0406040,0.0411923,0.0415478,0.0416667,0.0415478,0.0411923,0.0406040,0.0397887,0.0387550,0.0375132,0.0360760,0.0344581,0.0326756,0.0307464,0.0286897,0.0265258,0.0242759,0.0219617,0.0196053,0.0172290,0.0148548,0.0125044,0.0101987,0.0079577,0.0058006,0.0037448,0.0018064,0.0000000,-0.0016619,-0.0031686,-0.0045116,-0.0056841,-0.0066819,-0.0075026,-0.0081462,-0.0086145,-0.0089115,-0.0090431,-0.0090168,-0.0088419,-0.0085294,-0.0080912,-0.0075405,-0.0068916,-0.0061593,-0.0053590,-0.0045064,-0.0036172,-0.0027069,-0.0017910,-0.0008840,0.0000000,0.0008479,0.0016477,0.0023885,0.0030607,0.0036561,0.0041681,0.0045915,0.0049226,0.0051593,0.0053011,0.0053489,0.0053052,0.0051736,0.0049591,0.0046679,0.0043073,0.0038851,0.0034103,0.0028922,0.0023405,0.0017654,0.0011769,0.0005852,0.0000000,-0.0005691,-0.0011133,-0.0016242,-0.0020941,-0.0025166,-0.0028856,-0.0031966,-0.0034458,-0.0036306,-0.0037496,-0.0038022,-0.0037894,-0.0037128,-0.0035752,-0.0033802,-0.0031326,-0.0028374,-0.0025009,-0.0021294,-0.0017299,-0.0013098,-0.0008764,-0.0004373,0.0000000,0.0004283,0.0008407,0.0012304,0.0015915,0.0019186,0.0022067,0.0024518,0.0026506,0.0028008,0.0029006,0.0029494,0.0029473,0.0028953,0.0027951,0.0026494,0.0024613,0.0022348,0.0019744,0.0016850,0.0013720,0.0010411,0.0006982,0.0003491,0.0000000,-0.0003434,-0.0006753,-0.0009903,-0.0012835,-0.0015502,-0.0017863,-0.0019884,-0.0021536,-0.0022797,-0.0023651,-0.0024091,-0.0024114,-0.0023728,-0.0022945,-0.0021784,-0.0020269,-0.0018433,-0.0016310,-0.0013941,-0.0011368,-0.0008639,-0.0005802,-0.0002905,0.0000000,0.0002865,0.0005643,0.0008287,0.0010754,0.0013005,0.0015005,0.0016724,0.0018136,0.0019221,0.0019965,0.0020360,0.0020404,0.0020101,0.0019460,0.0018496,0.0017229,0.0015685,0.0013894,0.0011888,0.0009705,0.0007383,0.0004963,0.0002488,0.0000000,-0.0002458,-0.0004846,-0.0007124,-0.0009253,-0.0011201,-0.0012936,-0.0014430,-0.0015663,-0.0016615,-0.0017273,-0.0017631,-0.0017684,-0.0017436,-0.0016894,-0.0016070,-0.0014982,-0.0013650,-0.0012101,-0.0010362,-0.0008466,-0.0006445,-0.0004336,-0.0002175,0.0000000,0.0002153,0.0004247,0.0006247,0.0008120,0.0009836,0.0011368,0.0012690,0.0013783,0.0014631,0.0015221,0.0015546,0.0015603,0.0015394,0.0014925,0.0014207,0.0013253,0.0012083,0.0010718,0.0009184,0.0007507,0.0005719,0.0003850,0.0001932,0.0000000,-0.0001915,-0.0003779,-0.0005562,-0.0007234,-0.0008768,-0.0010139,-0.0011324,-0.0012306,-0.0013070,-0.0013605,-0.0013902,-0.0013961,-0.0013781,-0.0013368,-0.0012731,-0.0011882,-0.0010838,-0.0009619,-0.0008246,-0.0006744,-0.0005140,-0.0003462,-0.0001738,0.0000000,0.0001724,0.0003404,0.0005013,0.0006523,0.0007909,0.0009150,0.0010224,0.0011116,0.0011810,0.0012299,0.0012573,0.0012631,0.0012474,0.0012105,0.0011533,0.0010768,0.0009826,0.0008724,0.0007482,0.0006121,0.0004667,0.0003144,0.0001580,0.0000000,-0.0001568,-0.0003097,-0.0004562,-0.0005939,-0.0007204,-0.0008336,-0.0009319,-0.0010135,-0.0010772,-0.0011221,-0.0011476,-0.0011533,-0.0011393,-0.0011060,-0.0010541,-0.0009845,-0.0008987,-0.0007982,-0.0006847,-0.0005604,-0.0004274,-0.0002881,-0.0001448,0.0000000,0.0001438,0.0002841,0.0004186,0.0005451,0.0006613,0.0007656,0.0008560,0.0009313,0.0009902,0.0010318,0.0010555,0.0010610,0.0010485,0.0010181,0.0009706,0.0009068,0.0008280,0.0007356,0.0006312,0.0005167,0.0003942,0.0002658,0.0001336,0.0000000,-0.0001327,-0.0002624,-0.0003867,-0.0005037,-0.0006113,-0.0007078,-0.0007916,-0.0008615,-0.0009161,-0.0009549,-0.0009770,-0.0009824,-0.0009710,-0.0009431,-0.0008993,-0.0008404,-0.0007676,-0.0006821,-0.0005854,-0.0004794,-0.0003658,-0.0002467,-0.0001240,0.0000000,0.0001233,0.0002437,0.0003593,0.0004681,0.0005683,0.0006581,0.0007362,0.0008014,0.0008524,0.0008886,0.0009095,0.0009147,0.0009043,0.0008785,0.0008378,0.0007831,0.0007154,0.0006358,0.0005458,0.0004471,0.0003412,0.0002301,0.0001157,0.0000000,-0.0001151,-0.0002276,-0.0003356,-0.0004372,-0.0005309,-0.0006150,-0.0006881,-0.0007491,-0.0007970,-0.0008310,-0.0008506,-0.0008557,-0.0008461,-0.0008221,-0.0007842,-0.0007332,-0.0006698,-0.0005954,-0.0005113,-0.0004188,-0.0003197,-0.0002157,-0.0001085,0.0000000,0.0001079,0.0002134,0.0003148,0.0004102,0.0004981,0.0005771,0.0006459,0.0007032,0.0007483,0.0007804,0.0007990,0.0008038,0.0007949,0.0007725,0.0007370,0.0006892,0.0006298,0.0005599,0.0004808,0.0003939,0.0003008,0.0002029,0.0001021,0.0000000,-0.0001016,-0.0002009,-0.0002964,-0.0003863,-0.0004692,-0.0005437,-0.0006085,-0.0006627,-0.0007052,-0.0007356,-0.0007532,-0.0007579,-0.0007496,-0.0007286,-0.0006952,-0.0006502,-0.0005942,-0.0005284,-0.0004538,-0.0003719,-0.0002839,-0.0001916,-0.0000964};

    //Filter Order: 480 Sampling Frequency (Hz): 48000.000000 Cut-Off Frequency Lo (Hz): 2000.000000 Cut-Off Frequency Hi (Hz): 20.000000
    private double[] coefficients = {0.0000000,-0.0017529,-0.0034599,-0.0050018,-0.0062651,-0.0071503,-0.0075788,-0.0074991,-0.0068916,-0.0057713,-0.0041883,-0.0022266,0.0000000,0.0023538,0.0046810,0.0068206,0.0086145,0.0099182,0.0106103,0.0106022,0.0098452,0.0083363,0.0061213,0.0032954,0.0000000,-0.0035819,-0.0072343,-0.0107181,-0.0137832,-0.0161823,-0.0176839,-0.0180861,-0.0172290,-0.0150053,-0.0113682,-0.0063373,0.0000000,0.0074895,0.0159155,0.0250088,0.0344581,0.0439234,0.0530516,0.0614927,0.0689161,0.0750264,0.0795775,0.0823847,0.0833333,0.0823847,0.0795775,0.0750264,0.0689161,0.0614927,0.0530516,0.0439234,0.0344581,0.0250088,0.0159155,0.0074895,0.0000000,-0.0063373,-0.0113682,-0.0150053,-0.0172290,-0.0180861,-0.0176839,-0.0161823,-0.0137832,-0.0107181,-0.0072343,-0.0035819,0.0000000,0.0032954,0.0061213,0.0083363,0.0098452,0.0106022,0.0106103,0.0099182,0.0086145,0.0068206,0.0046810,0.0023538,0.0000000,-0.0022266,-0.0041883,-0.0057713,-0.0068916,-0.0074991,-0.0075788,-0.0071503,-0.0062651,-0.0050018,-0.0034599,-0.0017529,0.0000000,0.0016813,0.0031831,0.0044133,0.0053012,0.0058012,0.0058946,0.0055902,0.0049226,0.0039488,0.0027441,0.0013964,0.0000000,-0.0013506,-0.0025670,-0.0035727,-0.0043073,-0.0047302,-0.0048229,-0.0045890,-0.0040539,-0.0032620,-0.0022736,-0.0011603,0.0000000,0.0011286,0.0021507,0.0030011,0.0036272,0.0039930,0.0040809,0.0038919,0.0034458,0.0027788,0.0019409,0.0009926,0.0000000,-0.0009692,-0.0018506,-0.0025871,-0.0031326,-0.0034546,-0.0035368,-0.0033787,-0.0029964,-0.0024202,-0.0016931,-0.0008672,0.0000000,0.0008493,0.0016240,0.0022735,0.0027566,0.0030442,0.0031207,0.0029851,0.0026506,0.0021436,0.0015015,0.0007700,0.0000000,-0.0007558,-0.0014469,-0.0020277,-0.0024613,-0.0027209,-0.0027922,-0.0026736,-0.0023764,-0.0019238,-0.0013488,-0.0006923,0.0000000,0.0006809,0.0013045,0.0018299,0.0022231,0.0024597,0.0025263,0.0024210,0.0021536,0.0017448,0.0012243,0.0006289,0.0000000,-0.0006194,-0.0011877,-0.0016673,-0.0020269,-0.0022443,-0.0023066,-0.0022120,-0.0019690,-0.0015963,-0.0011208,-0.0005761,0.0000000,0.0005682,0.0010901,0.0015312,0.0018626,0.0020635,0.0021221,0.0020362,0.0018136,0.0014711,0.0010335,0.0005315,0.0000000,-0.0005247,-0.0010073,-0.0014156,-0.0017229,-0.0019097,-0.0019649,-0.0018863,-0.0016809,-0.0013641,-0.0009588,-0.0004933,0.0000000,0.0004875,0.0009362,0.0013163,0.0016027,0.0017772,0.0018294,0.0017569,0.0015663,0.0012716,0.0008941,0.0004602,0.0000000,-0.0004552,-0.0008745,-0.0012299,-0.0014982,-0.0016620,-0.0017113,-0.0016442,-0.0014663,-0.0011909,-0.0008377,-0.0004313,0.0000000,0.0004269,0.0008204,0.0011543,0.0014065,0.0015607,0.0016076,0.0015450,0.0013783,0.0011198,0.0007879,0.0004058,0.0000000,-0.0004019,-0.0007726,-0.0010873,-0.0013253,-0.0014711,-0.0015158,-0.0014572,-0.0013003,-0.0010567,-0.0007437,-0.0003832,0.0000000,0.0003797,0.0007301,0.0010278,0.0012530,0.0013912,0.0014338,0.0013788,0.0012306,0.0010004,0.0007042,0.0003629,0.0000000,-0.0003598,-0.0006920,-0.0009744,-0.0011882,-0.0013196,-0.0013603,-0.0013084,-0.0011681,-0.0009497,-0.0006687,-0.0003447,0.0000000,0.0003418,0.0006577,0.0009263,0.0011298,0.0012550,0.0012939,0.0012448,0.0011116,0.0009039,0.0006366,0.0003282,0.0000000,-0.0003256,-0.0006266,-0.0008827,-0.0010768,-0.0011964,-0.0012338,-0.0011871,-0.0010602,-0.0008624,-0.0006075,-0.0003132,0.0000000,0.0003109,0.0005983,0.0008430,0.0010286,0.0011430,0.0011789,0.0011346,0.0010135,0.0008245,0.0005809,0.0002996,0.0000000,-0.0002974,-0.0005725,-0.0008067,-0.0009845,-0.0010942,-0.0011288,-0.0010864,-0.0009706,-0.0007898,-0.0005565,-0.0002871,0.0000000,0.0002851,0.0005488,0.0007735,0.0009441,0.0010494,0.0010827,0.0010422,0.0009313,0.0007578,0.0005341,0.0002755,0.0000000,-0.0002737,-0.0005270,-0.0007428,-0.0009068,-0.0010081,-0.0010402,-0.0010015,-0.0008950,-0.0007284,-0.0005134,-0.0002649,0.0000000,0.0002632,0.0005069,0.0007145,0.0008724,0.0009699,0.0010010,0.0009638,0.0008615,0.0007012,0.0004943,0.0002551,0.0000000,-0.0002535,-0.0004882,-0.0006883,-0.0008404,-0.0009345,-0.0009646,-0.0009289,-0.0008303,-0.0006759,-0.0004765,-0.0002459,0.0000000,0.0002445,0.0004709,0.0006640,0.0008108,0.0009017,0.0009307,0.0008964,0.0008014,0.0006524,0.0004600,0.0002374,0.0000000,-0.0002361,-0.0004547,-0.0006413,-0.0007831,-0.0008710,-0.0008992,-0.0008661,-0.0007743,-0.0006305,-0.0004446,-0.0002295,0.0000000,0.0002282,0.0004397,0.0006201,0.0007573,0.0008424,0.0008697,0.0008378,0.0007491,0.0006100,0.0004301,0.0002221,0.0000000,-0.0002209,-0.0004255,-0.0006002,-0.0007332,-0.0008156,-0.0008421,-0.0008112,-0.0007254,-0.0005908,-0.0004166,-0.0002151,0.0000000,0.0002140,0.0004123,0.0005816,0.0007105,0.0007904,0.0008162,0.0007864,0.0007032,0.0005727,0.0004039,0.0002086,0.0000000,-0.0002075,-0.0003999,-0.0005641,-0.0006892,-0.0007667,-0.0007918,-0.0007629,-0.0006823,-0.0005558,-0.0003920,-0.0002024,0.0000000,0.0002014,0.0003882,0.0005476,0.0006691,0.0007445,0.0007689,0.0007409,0.0006627,0.0005398,0.0003808,0.0001966,0.0000000,-0.0001957,-0.0003771,-0.0005321,-0.0006502,-0.0007234,-0.0007472,-0.0007201,-0.0006441,-0.0005247,-0.0003701,-0.0001911};


    private double[] buffer;

    private int position = 0;
    private int index = 0;
    private double output = 0;

    public LowPassFilter() {
        this.buffer = new double[SAMPLE_SIZE];
        for(int i = 0; i < SAMPLE_SIZE; i++) this.buffer[i] = 0;
    }

    public double filter(double input) {
        output = 0;

        buffer[position] = input;
        index = position;
        for(int i = 0; i < SAMPLE_SIZE; i++) {
            output += coefficients[i] * buffer[index];
            if (--index < 0) index = SAMPLE_SIZE - 1;

        }
        if(++position >= SAMPLE_SIZE) position = 0;
        return output;
    }
}
