/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * Seven Signs Record Update
 * 
 * packet type id 0xf5
 * format:
 * 
 * c cc	(Page Num = 1 -> 4, period)
 * 
 * 1: [ddd cc dd ddd c ddd c]
 * 2: [hc [cd (dc (S))]
 * 3: [ccc (cccc)]
 * 4: [(cchh)]
 * 
 * @author Tempy
 *  
 */
public class RecordUpdate extends ServerBasePacket
{
    private static Logger _log = Logger.getLogger(RecordUpdate.class.getName());

    private static final String _S__F5_RecordUpdate = "[S] F5 RecordUpdate";
    private L2PcInstance _player;
    private int _page;

    public RecordUpdate(L2PcInstance player, int recordPage)
    {
        _player = player;
        _page = recordPage;
    }

    final void runImpl()
    {
        // no long-running tasks
    }

    final void writeImpl()
    {
        writeC(0xf5);

        writeC(_page);
        writeC(SevenSigns.getInstance().getCurrentPeriod()); // current period?

        switch (_page)
        {
            case 1:
                // [ddd cc dd ddd c ddd c]	
                writeD(SevenSigns.getInstance().getCurrentCycle());

                int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();

                switch (currentPeriod)
                {
                    case SevenSigns.PERIOD_COMP_RECRUITING:
                        writeD(1183);
                        break;
                    case SevenSigns.PERIOD_COMPETITION:
                        writeD(1176);
                        break;
                    case SevenSigns.PERIOD_COMP_RESULTS:
                        writeD(1184);
                        break;
                    case SevenSigns.PERIOD_SEAL_VALIDATION:
                        writeD(1177);
                        break;
                }

                switch (currentPeriod)
                {
                    case SevenSigns.PERIOD_COMP_RECRUITING:
                    case SevenSigns.PERIOD_COMP_RESULTS:
                        writeD(1287);
                        break;
                    case SevenSigns.PERIOD_COMPETITION:
                    case SevenSigns.PERIOD_SEAL_VALIDATION:
                        writeD(1286);
                        break;
                }

                writeC(SevenSigns.getInstance().getPlayerCabal(_player));
                writeC(SevenSigns.getInstance().getPlayerSeal(_player));

                writeD(SevenSigns.getInstance().getPlayerStoneContrib(_player)); // Seal Stones Turned-In
                writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_player)); // Ancient Adena to Collect

                int dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DAWN);
                int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DAWN);
                int dawnTotalScore = dawnStoneScore + dawnFestivalScore;

                int duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DUSK);
                int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DUSK);
                int duskTotalScore = duskStoneScore + duskFestivalScore;

                int totalStoneScore = duskStoneScore + dawnStoneScore;
                totalStoneScore = (totalStoneScore == 0) ? 1 : totalStoneScore; // Prevents divide by zero errors when competition begins.

                /*
                 * Scoring seems to be proportionate to a set base value, so base this on 
                 * the maximum obtainable score from festivals, which is 500.
                 */
                int duskStoneScoreProp = (duskStoneScore * 500 / totalStoneScore);   
                int dawnStoneScoreProp = (dawnStoneScore * 500 / totalStoneScore);

                int totalOverallScore = duskTotalScore + dawnTotalScore;
                totalOverallScore = (totalOverallScore == 0) ? 1 : totalOverallScore; // Prevents divide by zero errors when competition begins.

                int dawnPercent = (dawnTotalScore * 100 / totalOverallScore);
                int duskPercent = (duskTotalScore * 100 / totalOverallScore);

                if (Config.DEBUG) {
                    _log.info("Dusk Stone Score: " + duskStoneScore + " - Dawn Stone Score: "
                              + dawnStoneScore);
                    _log.info("Dusk Festival Score: " + duskFestivalScore + " - Dawn Festival Score: "
                              + dawnFestivalScore);
                    _log.info("Dusk Score: " + duskTotalScore + " - Dawn Score: " + dawnTotalScore);
                    _log.info("Overall Score: " + totalOverallScore);
                    _log.info("");
                    _log.info("Dusk Prop: " + ((duskStoneScore / totalStoneScore) * 500) + " - Dawn Prop: "
                              + ((dawnStoneScore / totalStoneScore) * 500));
                    _log.info("Dusk %: " + duskPercent + " - Dawn %: " + dawnPercent);
                }

                /* DUSK */
                writeD(duskStoneScoreProp); // Seal Stone Score
                writeD(duskFestivalScore); // Festival Score
                writeD(duskStoneScoreProp + duskFestivalScore); // Total Score

                writeC(duskPercent); // Dusk %

                /* DAWN */
                writeD(dawnStoneScoreProp); // Seal Stone Score
                writeD(dawnFestivalScore); // Festival Score
                writeD(dawnStoneScoreProp + dawnFestivalScore); // Total Score

                writeC(dawnPercent); // Dawn %
                break;
            case 2:
                // c cc hc [cd (dc (S))]
                if (SevenSigns.getInstance().isSealValidationPeriod()) writeH(0);
                else writeH(1);
                
                writeC(5); // Total number of festivals
                
                for (int i = 0; i < 5; i++)
                {
                    writeC(i + 1); // Current client-side festival ID
                    writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
                    
                    int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
                    int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);
                    
                    // Dusk Score \\
                    writeD(duskScore);
                    
                    StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, i);
                    String[] partyMembers = highScoreData.getString("members").split(",");
                    
                    if (partyMembers != null)
                    {
                        writeC(partyMembers.length);
                        
                        for (String partyMember : partyMembers) 
                            writeS(partyMember);
                    }
                    else {
                        writeC(0);
                    }
                    
                    // Dawn Score \\
                    writeD(dawnScore);
                    
                    highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, i);
                    partyMembers = highScoreData.getString("members").split(",");
                    
                    if (partyMembers != null)
                    {
                        writeC(partyMembers.length);
                        
                        for (String partyMember : partyMembers) 
                            writeS(partyMember);
                    }
                    else {
                        writeC(0);
                    }
                }
                break;
            case 3:
                // c cc [ccc (cccc)]
                writeC(10); // Minimum limit for winning cabal to retain their seal
                writeC(35); // Minimum limit for winning cabal to claim a seal
                writeC(3); // Total number of seals

                for (int i = 1; i < 4; i++)
                {
                    int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
                    int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
                    int totalProportion = dawnProportion + duskProportion;

                    totalProportion = (totalProportion < 1) ? 1 : totalProportion; // Prevents divide by zero errors.

                    if (Config.DEBUG) 
                        _log.info(SevenSigns.getSealName(i, true) + " = Dawn Prop: " + dawnProportion + "("
                        + ((dawnProportion / totalProportion) * 100) + "%)" + ", Dusk Prop: "
                        + duskProportion + "(" + ((duskProportion / totalProportion) * 100) + "%)");

                    writeC(i);
                    writeC(SevenSigns.getInstance().getSealOwner(i));
                    writeC((dawnProportion * 100 / totalProportion));
                    writeC((duskProportion * 100 / totalProportion));
                }
                break;
            case 4:
                // c cc [(cchh)]

                writeC(SevenSigns.getInstance().getCabalHighestScore()); // Overall predicted winner
                writeC(3); // Total number of seals

                for (int i = 1; i < 4; i++)
                {
                    int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
                    int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
                    int totalProportion = dawnProportion + duskProportion;

                    int sealOwner = SevenSigns.getInstance().getSealOwner(i);

                    writeC(i);
                    writeC(sealOwner);

                    /*
                     * 1289 = 10% or more voted for owned seal
                     * 1290 = 35% or more voted for non-owned seal
                     * 1291 = 10% or less voted for owned seal
                     * 1292 = 35% or less voted for non-owned seal
                     */
                    if (sealOwner != SevenSigns.CABAL_NULL)
                    {
                        if (totalProportion >= 10) writeH(1289);
                        else writeH(1291);
                    }
                    else
                    {
                        if (totalProportion >= 35) writeH(1290);
                        else writeH(1292);
                    }

                    // Shows a short description of the seal status when not Seal Validation.
                    if (SevenSigns.getInstance().isSealValidationPeriod()) writeH(1);
                    else writeH(0);
                }

                break;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__F5_RecordUpdate;
    }
}