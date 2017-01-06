/*
 * Copyright (C) 2016 larryTheHarry 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
package larryTheCoder.database.purger;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author larryTheCoder
 */
public class TeamData implements Cloneable {

    public String name;
    public String leader;
    public ArrayList<String> members = new ArrayList<>();
    
    public TeamData(String name, String leader, String member){
        this.name = name;

        this.leader = leader;
        String[] hMem = member.split(",");
        this.members.addAll(Arrays.asList(hMem));
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            // This should never happends
            throw new CloneNotSupportedException();
        }
    }

}
