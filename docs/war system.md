## Clan Wars
### War Declaration 
The "clan wars" functionality is a system in place where any clan of Tier I or above is able to declare war on any other clan of Tier I or above using the `/war <target faction ID>` command. When the command has been issued a **configurable grace period** of for instance `22 hours` will begin before the war between the clans is activated, during this grace period the targeted faction has an opportunity to run the `/war start` whenever they want to start the war earlier at their convenience. Any clan can only be the target of a singular war declaration and participate in a singular war at a time, any war from the point of activation lasts a **configurable amount of time** of for instance `4 hours` after which the war ends. 

### Peace Treaties
Throughout the duration of the war any of the participating clans members with the sufficient clan permissions have the opportunity to issue the `/war peace` that has a cooldown of a **configurable amount of time** which notifies the enemy clans members that an option to end the war earlier has been offered which can be accepted by running the `/war peace accept` command with the sufficient clan permissions, the offer can alternatively be declined by running the command `/war peace decline` or by simply ignoring the offer for a **configurable amount of time** for instance. If both clans members have run the command then the war will end immediately. Alternatively money can be offered or demanded for a peace treaty by using either the command `/war peace <amount>` where the `<amount>` can be a negative or as positive number depending on whether it is a offer or a demand. 

Ex. `/war peace -5000` to offer $5000 to the enemy clan to end the war.

Ex. `/war peace 7000` to demand $7000 from the enemy clan to end the war.

### Chunk Occupation 
During the war the PVP, PVE, and end crystal damage protections for each clan in their claimed chunks is disabled for the participating clan members. Any members of the participating clans are during the duration of the war able to "occupy" the enemy clans claimed chunks by standing unopposed in said chunk for a **configurable amount of time**, for instance `10 minutes`. This occupation is illustrated by a progress bar in the form of a boss health bar at the top of your screen with it's title naming the status of the current claim chunk, see below for a list of all occupation statuses. Any claimed chunk can only be occupied if any one of it's orthogonally adjacent claim chunks are not **controlled** by the defenders otherwise the chunk is considered **secured**. 

- **Controlled**: Completely under the control of the defenders with the progress bar 100% full and green.  
- **Capturing**: Currently being overtaken by the attackers with the progress bar depleting and red. 
- **Contested**: Currently being fought over by both attackers and defenders with the progress bar static and gray. 
- **Captured**: Completely under the control of the attackers with the progress bar 0% full and red. 
- **Liberating**: Currently being retaken under control of the defenders with the progress bar replenishing and green. 
- **Secured**: Currently uncapturable and completely under the control of the defenders with the progress bar 100% full and blue. 

When a claim chunk is **captured** or currently being **liberated** it does not have **any** of it's protections against war participating members of the attacking clan, meaning the attackers can freely break and place blocks as well as interact with everything. If all claimed chunks of any faction during a war are **captured** then that faction will be disbanded, it's claims deleted, and the war ended. The leader of the attacking clan will receive money equal to the total amount spent on the disbanded clans tier upgrades and claim chunks.     

### Post War
When war has ended the clan that was declared on will be protected from any other incoming war declarations for a **configurable amount of time**, for instance `48 hours`. 
