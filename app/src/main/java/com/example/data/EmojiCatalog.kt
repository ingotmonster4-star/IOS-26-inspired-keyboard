package com.example.data

data class EmojiCategorySpec(
    val id: String,
    val icon: String,
    val title: String,
    val emojis: List<EmojiItem>,
)

data class EmojiItem(
    val emoji: String,
    val name: String,
    val keywords: String = "",
    val supportsSkinTone: Boolean = false,
)

data class EmoticonGroupSpec(
    val id: String,
    val title: String,
    val items: List<String>,
)

object EmojiCatalog {

    val skinToneModifiers: List<String> = listOf(
        "🏻", // Light
        "🏼", // Medium-Light
        "🏽", // Medium
        "🏾", // Medium-Dark
        "🏿", // Dark
    )

    fun skinToneVariants(baseEmoji: String): List<String> {
        val zwj = "\u200D"
        val vs16 = "\uFE0F"
        val cleaned = if (baseEmoji.contains(zwj)) {
            val parts = baseEmoji.split(zwj, limit = 2)
            val first = parts[0].removeSuffix(vs16)
            return listOf(baseEmoji) + skinToneModifiers.map { tone -> "$first$tone$zwj${parts[1]}" }
        } else {
            baseEmoji.removeSuffix(vs16)
        }
        return listOf(baseEmoji) + skinToneModifiers.map { tone -> "$cleaned$tone" }
    }

    private fun parseItems(raw: String, skinTone: Boolean = false): List<EmojiItem> {
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split('|')
                val emoji = parts.getOrNull(0)?.trim().orEmpty()
                val name = parts.getOrNull(1)?.trim().orEmpty()
                if (emoji.isEmpty() || name.isEmpty()) return@mapNotNull null
                val extra = parts.getOrNull(2)?.trim().orEmpty()
                val keywords = (name.lowercase() + " " + extra.lowercase()).trim()
                EmojiItem(
                    emoji = emoji,
                    name = name,
                    keywords = keywords,
                    supportsSkinTone = skinTone,
                )
            }
            .toList()
    }

    private fun flagFromIso(code: String): String {
        if (code.length != 2) return ""
        val c1 = Character.toChars(0x1F1E6 + (code[0].uppercaseChar() - 'A'))
        val c2 = Character.toChars(0x1F1E6 + (code[1].uppercaseChar() - 'A'))
        return String(c1) + String(c2)
    }

    private val smileysEmojis: List<EmojiItem> = parseItems(
        """
        😀|Grinning Face|happy smile joy
        😃|Grinning Face with Big Eyes|happy smile joy
        😄|Grinning Face with Smiling Eyes|happy smile laugh
        😁|Beaming Face with Smiling Eyes|grin smile
        😆|Grinning Squinting Face|laugh lol xd
        😅|Grinning Face with Sweat|nervous laugh relief
        🤣|Rolling on the Floor Laughing|lol lmao rofl
        😂|Face with Tears of Joy|lol laugh crying happy
        🙂|Slightly Smiling Face|smile
        🙃|Upside-Down Face|silly sarcasm
        🫠|Melting Face|hot embarrassed melt
        😉|Winking Face|wink flirt
        😊|Smiling Face with Smiling Eyes|blush happy cute
        😇|Smiling Face with Halo|angel innocent blessed
        🥰|Smiling Face with Hearts|love adore crush
        😍|Smiling Face with Heart-Eyes|love heart crush
        🤩|Star-Struck|excited amazed wow
        😘|Face Blowing a Kiss|kiss love flirt
        😗|Kissing Face|kiss
        ☺️|Smiling Face|blush relaxed
        😚|Kissing Face with Closed Eyes|kiss
        😙|Kissing Face with Smiling Eyes|kiss
        🥲|Smiling Face with Tear|proud bittersweet touched
        😋|Face Savoring Food|yum delicious tongue
        😛|Face with Tongue|tongue silly
        😜|Winking Face with Tongue|crazy silly wink
        🤪|Zany Face|goofy wild crazy
        😝|Squinting Face with Tongue|prank silly
        🤑|Money-Mouth Face|rich dollar cash
        🤗|Smiling Face with Open Hands|hug care
        🤭|Face with Hand Over Mouth|giggle oops secret
        🫢|Face with Open Eyes and Hand Over Mouth|gasp shock omg
        🫣|Face with Peeking Eye|peek shy scared
        🤫|Shushing Face|quiet secret shh
        🤔|Thinking Face|hmm wonder ponder
        🫡|Saluting Face|salute yes sir respect
        🤐|Zipper-Mouth Face|secret quiet zip
        🤨|Face with Raised Eyebrow|skeptical sus doubt
        😐|Neutral Face|meh blank
        😑|Expressionless Face|blank deadpan
        😶|Face Without Mouth|speechless silent
        🫥|Dotted Line Face|invisible depressed hide
        😶‍🌫️|Face in Clouds|foggy dreamy
        😏|Smirking Face|smug flirt
        😒|Unamused Face|annoyed bored
        🙄|Face with Rolling Eyes|eye roll whatever
        😬|Grimacing Face|awkward yikes
        😮‍💨|Face Exhaling|relief sigh phew
        🤥|Lying Face|lie pinocchio
        🫨|Shaking Face|shocked vibrate dizzy
        🙂‍↔️|Head Shaking Horizontally|no shake
        🙂‍↕️|Head Shaking Vertically|yes nod
        😌|Relieved Face|calm peaceful
        😔|Pensive Face|sad thoughtful
        😪|Sleepy Face|tired snot
        🤤|Drooling Face|drool hungry
        😴|Sleeping Face|sleep zzz tired
        🫩|Face with Bags Under Eyes|exhausted tired
        😷|Face with Medical Mask|sick mask
        🤒|Face with Thermometer|sick fever
        🤕|Face with Head-Bandage|hurt injured
        🤢|Nauseated Face|sick gross green
        🤮|Face Vomiting|puke barf sick
        🤧|Sneezing Face|achoo cold tissue
        🥵|Hot Face|hot sweat spicy
        🥶|Cold Face|freezing ice blue
        🥴|Woozy Face|drunk dizzy
        😵|Face with Crossed-Out Eyes|dead dizzy knocked
        😵‍💫|Face with Spiral Eyes|hypnotized dizzy
        🤯|Exploding Head|mind blown shock
        🤠|Cowboy Hat Face|yeehaw hat western
        🥳|Partying Face|party celebrate birthday
        🥸|Disguised Face|glasses mustache incognito
        😎|Smiling Face with Sunglasses|cool shades
        🤓|Nerd Face|geek glasses smart
        🧐|Face with Monocle|fancy inspect hmm
        😕|Confused Face|unsure puzzled
        🫤|Face with Diagonal Mouth|meh unsure
        😟|Worried Face|anxious concerned
        🙁|Slightly Frowning Face|sad frown
        ☹️|Frowning Face|sad frown
        😮|Face with Open Mouth|wow surprised
        😯|Hushed Face|surprised quiet
        😲|Astonished Face|shocked wow
        😳|Flushed Face|blush embarrassed shocked
        🥺|Pleading Face|puppy eyes please uwu
        🥹|Face Holding Back Tears|emotional touched cry
        😦|Frowning Face with Open Mouth|shocked
        😧|Anguished Face|distressed
        😨|Fearful Face|scared fear
        😰|Anxious Face with Sweat|nervous panic
        😥|Sad but Relieved Face|phew sad
        😢|Crying Face|sad tear
        😭|Loudly Crying Face|sob cry tears sad
        😱|Face Screaming in Fear|scream omg horror
        😖|Confounded Face|frustrated
        😣|Persevering Face|struggle
        😞|Disappointed Face|sad letdown
        😓|Downcast Face with Sweat|tired stress
        😩|Weary Face|tired frustrated
        😫|Tired Face|exhausted whine
        🥱|Yawning Face|bored sleepy yawn
        😤|Face with Steam From Nose|mad triumph hmph
        😡|Enraged Face|angry mad red
        😠|Angry Face|mad grumpy
        🤬|Face with Symbols on Mouth|cursing swear mad
        😈|Smiling Face with Horns|devil naughty evil
        👿|Angry Face with Horns|devil mad
        💀|Skull|dead lol skeleton
        ☠️|Skull and Crossbones|pirate danger dead
        💩|Pile of Poo|poop funny
        🤡|Clown Face|clown circus funny
        👹|Ogre|monster mask
        👺|Goblin|mask monster
        👻|Ghost|boo halloween spooky
        👽|Alien|ufo space
        👾|Alien Monster|arcade game pixel
        🤖|Robot|bot ai tech
        😺|Grinning Cat|cat happy
        😸|Grinning Cat with Smiling Eyes|cat
        😹|Cat with Tears of Joy|cat laugh
        😻|Smiling Cat with Heart-Eyes|cat love
        😼|Cat with Wry Smile|cat smirk
        😽|Kissing Cat|cat kiss
        🙀|Weary Cat|cat shock
        😿|Crying Cat|cat sad
        😾|Pouting Cat|cat mad
        🙈|See-No-Evil Monkey|monkey shy oops
        🙉|Hear-No-Evil Monkey|monkey
        🙊|Speak-No-Evil Monkey|monkey oops
        💌|Love Letter|heart mail
        💘|Heart with Arrow|cupid love
        💝|Heart with Ribbon|gift love
        💖|Sparkling Heart|love sparkle
        💗|Growing Heart|love pulse
        💓|Beating Heart|love heartbeat
        💞|Revolving Hearts|love
        💕|Two Hearts|love
        💟|Heart Decoration|love
        ❣️|Heart Exclamation|love
        💔|Broken Heart|sad heartbreak
        ❤️‍🔥|Heart on Fire|passion love
        ❤️‍🩹|Mending Heart|healing love
        ❤️|Red Heart|love
        🩷|Pink Heart|love pink
        🧡|Orange Heart|love orange
        💛|Yellow Heart|love yellow
        💚|Green Heart|love green
        💙|Blue Heart|love blue
        🩵|Light Blue Heart|love cyan
        💜|Purple Heart|love purple
        🤎|Brown Heart|love brown
        🖤|Black Heart|dark emo love
        🩶|Grey Heart|silver love
        🤍|White Heart|pure love
        💋|Kiss Mark|lips kiss
        💯|Hundred Points|100 perfect keep it
        💢|Anger Symbol|mad comic
        💥|Collision|boom bang crash
        💫|Dizzy|star sparkle
        💦|Sweat Droplets|water drops splash
        💨|Dashing Away|fast wind fart
        🕳️|Hole|void hole
        💬|Speech Balloon|chat talk comment
        👁️‍🗨️|Eye in Speech Bubble|witness
        🗨️|Left Speech Bubble|chat
        🗯️|Right Anger Bubble|yell comic
        💭|Thought Balloon|think dream
        💤|ZZZ|sleep snore tired
        """
    )

    private val peopleHandsNoTone: List<EmojiItem> = parseItems(
        """
        🗣️|Speaking Head|talk voice
        👤|Bust in Silhouette|user profile
        👥|Busts in Silhouette|group friends users
        🫂|People Hugging|hug care comfort
        👣|Footprints|steps walk feet
        🧠|Brain|smart think mind
        🫀|Anatomical Heart|organ pulse
        🫁|Lungs|breath lungs
        🦷|Tooth|dentist smile
        🦴|Bone|skeleton dog
        👀|Eyes|look watch see
        👁️|Eye|see watch
        👅|Tongue|taste lick
        👄|Mouth|lips talk
        🫦|Biting Lip|flirt nervous
        """,
        skinTone = false,
    )

    private val peopleWithTone: List<EmojiItem> = parseItems(
        """
        👋|Waving Hand|hello hi bye
        🤚|Raised Back of Hand|hand stop
        🖐️|Hand with Fingers Splayed|five high five
        ✋|Raised Hand|stop high five
        🖖|Vulcan Salute|spock trek
        🫱|Rightwards Hand|hand reach
        🫲|Leftwards Hand|hand reach
        🫳|Palm Down Hand|drop dismiss
        🫴|Palm Up Hand|offer hold
        🫷|Leftwards Pushing Hand|push stop
        🫸|Rightwards Pushing Hand|push stop
        👌|OK Hand|ok nice perfect
        🤌|Pinched Fingers|italian chef kiss
        🤏|Pinching Hand|tiny small bit
        ✌️|Victory Hand|peace two
        🤞|Crossed Fingers|luck hope
        🫰|Hand with Index and Thumb Crossed|money snap heart
        🤟|Love-You Gesture|love rock
        🤘|Sign of the Horns|rock metal
        🤙|Call Me Hand|shaka call cool
        👈|Backhand Index Pointing Left|left point
        👉|Backhand Index Pointing Right|right point
        👆|Backhand Index Pointing Up|up point
        🖕|Middle Finger|rude finger
        👇|Backhand Index Pointing Down|down point
        ☝️|Index Pointing Up|one idea point
        🫵|Index Pointing at the Viewer|you point
        👍|Thumbs Up|like yes good ok
        👎|Thumbs Down|dislike no bad
        ✊|Raised Fist|power punch solidarity
        👊|Oncoming Fist|bro fist bump punch
        🤛|Left-Facing Fist|fist bump
        🤜|Right-Facing Fist|fist bump
        👏|Clapping Hands|clap applause bravo
        🙌|Raising Hands|hooray celebrate praise
        🫶|Heart Hands|love heart hands
        👐|Open Hands|jazz hands open
        🤲|Palms Up Together|pray cup hands
        🤝|Handshake|deal agree partner
        🙏|Folded Hands|please thank you pray namaste
        ✍️|Writing Hand|write draw sign
        💅|Nail Polish|slay manicure sassy
        🤳|Selfie|camera phone
        💪|Flexed Biceps|strong gym muscle flex
        🦾|Mechanical Arm|robot cyborg prosthetic
        🦿|Mechanical Leg|prosthetic robot
        🦵|Leg|kick leg
        🦶|Foot|kick foot
        👂|Ear|hear listen
        🦻|Ear with Hearing Aid|hear aid
        👃|Nose|smell nose
        👶|Baby|infant newborn
        🧒|Child|kid young
        👦|Boy|kid male
        👧|Girl|kid female
        🧑|Person|human adult
        👱|Person Blond Hair|blond
        👨|Man|male guy
        🧔|Person Beard|beard
        🧔‍♂️|Man Beard|beard
        🧔‍♀️|Woman Beard|beard
        👩|Woman|female lady
        🧓|Older Person|elder senior
        👴|Old Man|grandpa elder
        👵|Old Woman|grandma elder
        🙍|Person Frowning|sad frown
        🙎|Person Pouting|pout mad
        🙅|Person Gesturing No|no stop x
        🙆|Person Gesturing OK|ok yes
        💁|Person Tipping Hand|sassy help info
        🙋|Person Raising Hand|question me raise
        🧏|Deaf Person|sign language
        🙇|Person Bowing|sorry bow respect
        🤦|Person Facepalming|facepalm omg duh
        🤷|Person Shrugging|shrug idk dunno
        🧑‍⚕️|Health Worker|doctor nurse
        🧑‍🎓|Student|grad school study
        🧑‍🏫|Teacher|school professor
        🧑‍⚖️|Judge|law court
        🧑‍🌾|Farmer|farm nature
        🧑‍🍳|Cook|chef cooking
        🧑‍🔧|Mechanic|repair tool
        🧑‍🏭|Factory Worker|worker industry
        🧑‍💼|Office Worker|business job
        🧑‍🔬|Scientist|lab science
        🧑‍💻|Technologist|coder developer tech laptop
        🧑‍🎤|Singer|music star rock
        🧑‍🎨|Artist|paint art palette
        🧑‍✈️|Pilot|fly airplane
        🧑‍🚀|Astronaut|space rocket
        🧑‍🚒|Firefighter|fire hero
        👮|Police Officer|cop police
        🕵️|Detective|spy search
        💂|Guard|royal guard
        🥷|Ninja|stealth fighter
        👷|Construction Worker|build helmet
        🫅|Person with Crown|royal monarch
        🤴|Prince|royal crown
        👸|Princess|royal crown
        👳|Person Wearing Turban|turban
        👲|Person with Skullcap|hat
        🧕|Woman with Headscarf|hijab
        🤵|Person in Tuxedo|suit wedding
        👰|Person with Veil|bride wedding
        🤰|Pregnant Woman|baby mom
        🫃|Pregnant Man|pregnant
        🫄|Pregnant Person|pregnant
        🤱|Breast-Feeding|baby mom
        🧑‍🍼|Person Feeding Baby|bottle baby
        👼|Baby Angel|cherub halo
        🎅|Santa Claus|christmas santa
        🤶|Mrs. Claus|christmas
        🧑‍🎄|Mx Claus|christmas
        🦸|SuperHero|hero cape
        🦹|Supervillain|villain evil
        🧙|Mage|wizard magic
        🧚|Fairy|magic wings
        🧛|Vampire|dracula fangs
        🧜|Merperson|mermaid ocean
        🧝|Elf|fantasy elf
        🧞|Genie|lamp wish
        🧟|Zombie|undead halloween
        🧌|Troll|fantasy monster
        💆|Person Getting Massage|spa relax
        💇|Person Getting Haircut|salon hair
        🚶|Person Walking|walk stroll
        🧍|Person Standing|stand
        🧎|Person Kneeling|kneel
        🧑‍🦯|Person with White Cane|blind accessibility
        🧑‍🦼|Person in Motorized Wheelchair|wheelchair
        🧑‍🦽|Person in Manual Wheelchair|wheelchair
        🏃|Person Running|run sprint exercise
        💃|Woman Dancing|dance party salsa
        🕺|Man Dancing|dance disco party
        🕴️|Person in Suit Levitating|float spy
        👯|People with Bunny Ears|party dance
        🧖|Person in Steamy Room|sauna spa
        🧗|Person Climbing|climb rock
        🤺|Person Fencing|sword sport
        🏇|Horse Racing|jockey horse
        ⛷️|Skier|ski snow
        🏂|Snowboarder|snowboard winter
        🏌️|Person Golfing|golf
        🏄|Person Surfing|surf beach
        🚣|Person Rowing Boat|boat row
        🏊|Person Swimming|swim pool
        ⛹️|Person Bouncing Ball|basketball
        🏋️|Person Lifting Weights|gym lift workout
        🚴|Person Biking|bike cycle
        🚵|Person Mountain Biking|mountain bike
        🤸|Person Cartwheeling|gymnastics flip
        🤼|People Wrestling|wrestle fight
        🤽|Person Playing Water Polo|water polo
        🤾|Person Playing Handball|handball
        🤹|Person Juggling|juggle circus
        🧘|Person in Lotus Position|yoga meditate zen
        🛀|Person Taking Bath|bath tub relax
        🛌|Person in Bed|sleep hotel rest
        🧑‍🤝‍🧑|People Holding Hands|friends couple
        👭|Women Holding Hands|friends couple
        👫|Woman and Man Holding Hands|couple date
        👬|Men Holding Hands|friends couple
        💏|Kiss|love couple
        💑|Couple with Heart|love relationship
        👪|Family|home parents kids
        """,
        skinTone = true,
    )

    private val natureEmojis: List<EmojiItem> = parseItems(
        """
        🐶|Dog Face|puppy pet dog
        🐱|Cat Face|kitty pet cat
        🐭|Mouse Face|mouse
        🐹|Hamster|hamster cute
        🐰|Rabbit Face|bunny rabbit
        🦊|Fox|fox clever
        🐻|Bear|bear
        🐼|Panda|panda bear
        🐻‍❄️|Polar Bear|polar bear arctic
        🐨|Koala|koala australia
        🐯|Tiger Face|tiger
        🦁|Lion|lion king
        🐮|Cow Face|cow moo
        🐷|Pig Face|pig oink
        🐽|Pig Nose|snout
        🐸|Frog|frog toad
        🐵|Monkey Face|monkey
        🐒|Monkey|monkey
        🦍|Gorilla|ape gorilla
        🦧|Orangutan|ape
        🐕|Dog|dog pet
        🦮|Guide Dog|service dog
        🐕‍🦺|Service Dog|dog
        🐩|Poodle|dog
        🐺|Wolf|wolf howl
        🦊|Fox|fox
        🦝|Raccoon|trash panda
        🐈|Cat|cat
        🐈‍⬛|Black Cat|halloween cat
        🦁|Lion|lion
        🐅|Tiger|tiger
        🐆|Leopard|cat spot
        🐴|Horse Face|horse
        🫎|Moose|elk antler
        🫏|Donkey|mule
        🐎|Horse|racing horse
        🦄|Unicorn|magic rainbow
        🦓|Zebra|stripes
        🦌|Deer|bambi antlers
        🦬|Bison|buffalo
        🐮|Cow|cow
        🐂|Ox|ox bull
        🐃|Water Buffalo|buffalo
        🐄|Cow|cow milk
        🐷|Pig|pig
        🐖|Pig|pig
        🐗|Boar|pig wild
        🐏|Ram|sheep
        🐑|Ewe|sheep lamb
        🐐|Goat|goat greatest
        🐪|Camel|desert hump
        🐫|Two-Hump Camel|camel desert
        🦙|Llama|alpaca
        🦒|Giraffe|tall spots
        🐘|Elephant|elephant
        🦣|Mammoth|extinct tusk
        🦏|Rhinoceros|rhino
        🦛|Hippopotamus|hippo
        🐭|Mouse|mouse
        🐁|Mouse|rat
        🐀|Rat|rat
        🐹|Hamster|hamster
        🐰|Rabbit|bunny
        🐇|Rabbit|bunny
        🐿️|Chipmunk|squirrel
        🦫|Beaver|dam wood
        🦔|Hedgehog|spiky cute
        🦇|Bat|vampire halloween
        🐻|Bear|bear
        🦥|Sloth|lazy slow
        🦦|Otter|cute river
        🦨|Skunk|stink
        🦘|Kangaroo|australia hop
        🦡|Badger|honey badger
        🐾|Paw Prints|pet dog cat tracks
        🦃|Turkey|thanksgiving bird
        🐔|Chicken|hen farm
        🐓|Rooster|morning crow
        🐣|Hatching Chick|baby egg
        🐤|Baby Chick|bird yellow
        🐥|Front-Facing Baby Chick|chick
        🐦|Bird|tweet fly
        🐧|Penguin|cold arctic
        🕊️|Dove|peace bird white
        🦅|Eagle|usa bird fly
        🦆|Duck|quack duck
        🦢|Swan|lake graceful
        🦉|Owl|night wise hoot
        🦤|Dodo|bird extinct
        🪶|Feather|light quill
        🦩|Flamingo|pink bird
        🦚|Peacock|feathers fancy
        🦜|Parrot|pirate talk
        🪽|Wing|angel fly feather
        🐦‍⬛|Black Bird|crow raven
        🪿|Goose|honk silly goose
        🐦‍🔥|Phoenix|fire rebirth
        🐸|Frog|frog
        🐊|Crocodile|alligator gator
        🐢|Turtle|slow tortoise
        🦎|Lizard|gecko reptile
        🐍|Snake|slither hiss
        🐲|Dragon Face|dragon
        🐉|Dragon|fantasy fire
        🦕|Sauropod|dinosaur dino
        🦖|T-Rex|dinosaur dino
        🐳|Spouting Whale|whale ocean
        🐋|Whale|ocean sea
        🐬|Dolphin|sea ocean
        🦭|Seal|ocean cute
        🐟|Fish|fish swim
        🐠|Tropical Fish|nemo fish
        🐡|Blowfish|puffer fish
        🦈|Shark|jaws ocean
        🐙|Octopus|tentacles sea
        🐚|Spiral Shell|beach sea
        🪸|Coral|reef ocean
        🪼|Jellyfish|ocean sting
        🐌|Snail|slow shell
        🦋|Butterfly|wings pretty
        🐛|Bug|caterpillar worm
        🐜|Ant|insect bug
        🐝|Honeybee|bee honey buzz
        🪲|Beetle|bug insect
        🐞|Lady Beetle|ladybug luck
        🦗|Cricket|chirp bug
        🪳|Cockroach|bug
        🕷️|Spider|web halloween
        🕸️|Spider Web|halloween
        🦂|Scorpion|desert sting
        🦟|Mosquito|bite bug
        🪰|Fly|bug buzz
        🪱|Worm|earthworm
        🦠|Microbe|germ virus bacteria
        💐|Bouquet|flowers gift
        🌸|Cherry Blossom|sakura spring pink
        💮|White Flower|stamp flower
        🪷|Lotus|zen calm flower
        🏵️|Rosette|flower
        🌹|Rose|love red flower
        🥀|Wilted Flower|sad dead flower
        🌺|Hibiscus|tropical flower
        🌻|Sunflower|sun flower yellow
        🌼|Blossom|daisy flower
        🌷|Tulip|spring flower
        🪻|Hyacinth|purple flower
        🌱|Seedling|grow plant sprout
        🪴|Potted Plant|houseplant indoor
        🌲|Evergreen Tree|pine forest
        🌳|Deciduous Tree|tree green nature
        🌴|Palm Tree|beach tropical
        🌵|Cactus|desert spiky
        🌾|Sheaf of Rice|grain harvest
        🌿|Herb|leaf green
        ☘️|Shamrock|irish clover
        🍀|Four Leaf Clover|luck clover
        🍁|Maple Leaf|canada fall autumn
        🍂|Fallen Leaf|autumn fall
        🍃|Leaf Fluttering in Wind|breeze leaf
        🪹|Empty Nest|bird branch
        🪺|Nest with Eggs|bird eggs
        🍄|Mushroom|fungi toadstool
        🍄‍🟫|Brown Mushroom|fungi food
        🪨|Rock|stone boulder
        🪵|Wood|log timber
        ☀️|Sun|sunny bright
        🌤️|Sun Behind Small Cloud|partly cloudy
        ⛅|Sun Behind Cloud|cloudy
        🌥️|Sun Behind Large Cloud|overcast
        ☁️|Cloud|cloudy weather
        🌦️|Sun Behind Rain Cloud|rain sun
        🌧️|Cloud with Rain|rainy weather
        ⛈️|Cloud with Lightning and Rain|storm thunder
        🌩️|Cloud with Lightning|lightning storm
        🌨️|Cloud with Snow|snow winter
        ❄️|Snowflake|cold winter snow ice
        ☃️|Snowman|winter snow
        ⛄|Snowman Without Snow|winter frosty
        🌬️|Wind Face|breeze blow
        💨|Dashing Away|wind gust
        🌪️|Tornado|twister storm
        🌫️|Fog|mist fog
        🌈|Rainbow|pride color sky
        ☔|Umbrella with Rain Drops|rain umbrella
        ⚡|High Voltage|lightning zap energy fast
        🔥|Fire|lit flame hot
        💧|Droplet|water tear drop
        🌊|Water Wave|ocean surf sea
        🌑|New Moon|moon night
        🌒|Waxing Crescent Moon|moon
        🌓|First Quarter Moon|moon
        🌔|Waxing Gibbous Moon|moon
        🌕|Full Moon|moon night
        🌖|Waning Gibbous Moon|moon
        🌗|Last Quarter Moon|moon
        🌘|Waning Crescent Moon|moon
        🌙|Crescent Moon|night sleep moon
        🌚|New Moon Face|moon dark
        🌛|First Quarter Moon Face|moon
        🌜|Last Quarter Moon Face|moon
        🌝|Full Moon Face|moon
        🌞|Sun with Face|sun bright
        ⭐|Star|star favorite
        🌟|Glowing Star|sparkle star
        🌠|Shooting Star|wish star
        🌌|Milky Way|galaxy space night
        🪐|Ringed Planet|saturn space
        ☄️|Comet|space meteor
        ✨|Sparkles|magic clean shiny glow
        """
    )

    private val foodEmojis: List<EmojiItem> = parseItems(
        """
        🍇|Grapes|fruit wine
        🍈|Melon|fruit
        🍉|Watermelon|summer fruit
        🍊|Tangerine|orange fruit
        🍋|Lemon|sour citrus
        🍋‍🟩|Lime|green citrus
        🍌|Banana|fruit yellow
        🍍|Pineapple|tropical fruit
        🥭|Mango|tropical fruit
        🍎|Red Apple|apple fruit
        🍏|Green Apple|apple fruit
        🍐|Pear|fruit
        🍑|Peach|fruit
        🍒|Cherries|fruit cherry
        🍓|Strawberry|berry fruit
        🫐|Blueberries|berry fruit
        🥝|Kiwi Fruit|kiwi fruit
        🍅|Tomato|veggie sauce
        🫒|Olive|mediterranean
        🥥|Coconut|tropical palm
        🥑|Avocado|guacamole toast
        🍆|Eggplant|aubergine veggie
        🥔|Potato|spud fries
        🥕|Carrot|veggie orange
        🌽|Ear of Corn|corn cob
        🌶️|Hot Pepper|spicy chili
        🫑|Bell Pepper|capsicum veggie
        🥒|Cucumber|pickle veggie
        🥬|Leafy Green|lettuce kale
        🥦|Broccoli|green veggie
        🧄|Garlic|cooking flavor
        🧅|Onion|cooking cry
        🥜|Peanuts|nuts snack
        🫘|Beans|protein
        🌰|Chestnut|nut fall
        🫚|Ginger Root|spice
        🫛|Pea Pod|peas green
        🍞|Bread|loaf toast
        🥐|Croissant|french pastry breakfast
        🥖|Baguette Bread|french bread
        🫓|Flatbread|pita naan
        🥨|Pretzel|snack salty
        🥯|Bagel|breakfast bread
        🥞|Pancakes|breakfast syrup
        🧇|Waffle|breakfast syrup
        🧀|Cheese Wedge|cheese dairy
        🍖|Meat on Bone|bbq meat
        🍗|Poultry Leg|chicken drumstick
        🥩|Cut of Meat|steak beef
        🥓|Bacon|breakfast pork
        🍔|Hamburger|burger fast food
        🍟|French Fries|fries snack
        🍕|Pizza|slice italian cheese
        🌭|Hot Dog|sausage bun
        🥪|Sandwich|lunch bread
        🌮|Taco|mexican food
        🌯|Burrito|wrap mexican
        🫔|Tamale|mexican
        🥙|Stuffed Flatbread|kebab gyro falafel
        🧆|Falafel|chickpea
        🥚|Egg|breakfast
        🍳|Cooking|fried egg pan
        🥘|Shallow Pan of Food|paella curry
        🍲|Pot of Food|stew soup
        🫕|Fondue|cheese chocolate
        🥣|Bowl with Spoon|cereal oatmeal
        🥗|Green Salad|healthy salad
        🍿|Popcorn|movie snack
        🧈|Butter|dairy toast
        🧂|Salt|seasoning
        🥫|Canned Food|can soup
        🍱|Bento Box|japanese lunch
        🍘|Rice Cracker|japanese
        🍙|Rice Ball|onigiri japanese
        🍚|Cooked Rice|rice bowl
        🍛|Curry Rice|indian japanese curry
        🍜|Steaming Bowl|ramen noodles pho
        🍝|Spaghetti|pasta italian
        🍠|Roasted Sweet Potato|yam
        🍢|Oden|skewer
        🍣|Sushi|fish japanese
        🍤|Fried Shrimp|tempura prawn
        🍥|Fish Cake with Swirl|narutomaki
        🥮|Moon Cake|festival
        🍡|Dango|japanese sweet
        🥟|Dumpling|gyoza pierogi
        🥠|Fortune Cookie|luck chinese
        🥡|Takeout Box|chinese food
        🦪|Oyster|seafood pearl
        🍦|Soft Ice Cream|cone dessert
        🍧|Shaved Ice|summer dessert
        🍨|Ice Cream|bowl dessert
        🍩|Doughnut|donut sweet breakfast
        🍪|Cookie|chocolate chip snack
        🎂|Birthday Cake|party celebration
        🍰|Shortcake|cake slice dessert
        🧁|Cupcake|muffin sweet
        🥧|Pie|thanksgiving dessert
        🍫|Chocolate Bar|candy sweet
        🍬|Candy|sweet sugar
        🍭|Lollipop|candy sweet
        🍮|Custard|flan pudding
        🍯|Honey Pot|bee sweet
        🍼|Baby Bottle|milk baby
        🥛|Glass of Milk|dairy drink
        ☕|Hot Beverage|coffee espresso cafe tea
        🫖|Teapot|tea brew
        🍵|Teacup Without Handle|matcha green tea
        🍶|Sake|japanese drink
        🍾|Bottle with Popping Cork|champagne celebrate
        🍷|Wine Glass|red wine drink
        🍸|Cocktail Glass|martini bar
        🍹|Tropical Drink|vacation cocktail
        🍺|Beer Mug|beer cheers pub
        🍻|Clinking Beer Mugs|cheers beers
        🥂|Clinking Glasses|champagne toast cheers
        🥃|Tumbler Glass|whiskey bourbon
        🫗|Pouring Liquid|pour spill
        🥤|Cup with Straw|soda juice
        🧋|Bubble Tea|boba milk tea
        🧃|Beverage Box|juice box
        🧉|Mate|tea drink
        🧊|Ice|cold cube
        🥢|Chopsticks|asian utensils
        🍽️|Fork and Knife with Plate|dinner meal
        🍴|Fork and Knife|eat restaurant
        🥄|Spoon|soup utensil
        🫙|Jar|mason preserve
        🏺|Amphora|vase jar
        """
    )

    private val travelEmojis: List<EmojiItem> = parseItems(
        """
        🌍|Globe Showing Europe-Africa|earth world
        🌎|Globe Showing Americas|earth world
        🌏|Globe Showing Asia-Australia|earth world
        🌐|Globe with Meridians|internet web world
        🗺️|World Map|travel map
        🗾|Map of Japan|japan
        🧭|Compass|direction navigate
        🏔️|Snow-Capped Mountain|peak alps
        ⛰️|Mountain|hike climb
        🌋|Volcano|lava eruption
        🗻|Mount Fuji|japan mountain
        🏕️|Camping|tent outdoor
        🏖️|Beach with Umbrella|vacation ocean summer
        🏜️|Desert|sand cactus
        🏝️|Desert Island|tropical island
        🏞️|National Park|nature park
        🏟️|Stadium|sports concert arena
        🏛️|Classical Building|museum bank court
        🏗️|Building Construction|crane build
        🧱|Brick|wall build
        🛖|Hut|cabin island
        🏘️|Houses|neighborhood
        🏚️|Derelict House|old haunted
        🏠|House|home
        🏡|House with Garden|home yard
        🏢|Office Building|work corporate
        🏣|Japanese Post Office|mail
        🏤|Post Office|mail
        🏥|Hospital|medical doctor er
        🏦|Bank|money vault
        🏨|Hotel|travel stay
        🏩|Love Hotel|hotel
        🏪|Convenience Store|24h shop
        🏫|School|education study
        🏬|Department Store|shopping mall
        🏭|Factory|industry work
        🏯|Japanese Castle|shogun
        🏰|Castle|king queen medieval
        💒|Wedding|chapel marriage
        🗼|Tokyo Tower|tokyo japan
        🗽|Statue of Liberty|nyc usa
        ⛪|Church|chapel religion
        🕌|Mosque|islam religion
        🛕|Hindu Temple|temple religion
        🕍|Synagogue|judaism religion
        ⛩️|Shinto Shrine|torii japan
        🕋|Kaaba|mecca islam
        ⛲|Fountain|park water
        ⛺|Tent|camp outdoor
        🌁|Foggy|bridge mist
        🌃|Night with Stars|city night
        🏙️|Cityscape|skyline buildings
        🌄|Sunrise Over Mountains|morning dawn
        🌅|Sunrise|ocean morning
        🌆|Cityscape at Dusk|evening sunset
        🌇|Sunset|evening city
        🌉|Bridge at Night|golden gate night
        ♨️|Hot Springs|onsen steam
        🎠|Carousel Horse|carnival ride
        🛝|Playground Slide|park kids
        🎡|Ferris Wheel|carnival amusement
        🎢|Roller Coaster|theme park thrill
        💈|Barber Pole|haircut
        🎪|Circus Tent|carnival show
        🚂|Locomotive|steam train
        🚃|Railway Car|train
        🚄|High-Speed Train|bullet shinkansen
        🚅|Bullet Train|fast rail
        🚆|Train|transit commute
        🚇|Metro|subway underground
        🚈|Light Rail|tram train
        🚉|Station|train platform
        🚊|Tram|trolley
        🚝|Monorail|train
        🚞|Mountain Railway|rail
        🚋|Tram Car|trolley
        🚌|Bus|transit school
        🚍|Oncoming Bus|bus
        🚎|Trolleybus|bus
        🚐|Minibus|van
        🚑|Ambulance|emergency hospital
        🚒|Fire Engine|firetruck emergency
        🚓|Police Car|cop siren
        🚔|Oncoming Police Car|police
        🚕|Taxi|cab uber ride
        🚖|Oncoming Taxi|cab
        🚗|Automobile|car drive
        🚘|Oncoming Automobile|car
        🚙|Sport Utility Vehicle|suv car
        🛻|Pickup Truck|truck
        🚚|Delivery Truck|shipping package
        🚛|Articulated Lorry|semi truck
        🚜|Tractor|farm
        🏎️|Racing Car|f1 speed
        🏍️|Motorcycle|moto bike
        🛵|Motor Scooter|vespa moped
        🦽|Manual Wheelchair|accessibility
        🦼|Motorized Wheelchair|accessibility
        🛺|Auto Rickshaw|tuktuk
        🚲|Bicycle|bike cycle
        🛴|Kick Scooter|scooter
        🛹|Skateboard|skate
        🛼|Roller Skate|skate
        🚏|Bus Stop|transit
        🛣️|Motorway|highway road
        🛤️|Railway Track|train
        🛢️|Oil Drum|barrel
        ⛽|Fuel Pump|gas station petrol
        🛞|Wheel|tire car
        🚨|Police Car Light|siren alert emergency
        🚥|Horizontal Traffic Light|signal
        🚦|Vertical Traffic Light|signal go stop
        🛑|Stop Sign|stop halt
        🚧|Construction|roadwork wip
        ⚓|Anchor|ship sea boat
        🛟|Ring Buoy|lifesaver rescue
        ⛵|Sailboat|yacht sea
        🛶|Canoe|kayak paddle
        🚤|Speedboat|boat lake
        🛳️|Passenger Ship|cruise
        ⛴️|Ferry|boat
        🛥️|Motor Boat|boat
        🚢|Ship|cruise titanic
        ✈️|Airplane|flight travel fly
        🛩️|Small Airplane|cessna fly
        🛫|Airplane Departure|takeoff airport
        🛬|Airplane Arrival|landing airport
        🪂|Parachute|skydive
        💺|Seat|flight chair
        🚁|Helicopter|chopper fly
        🚟|Suspension Railway|rail
        🚠|Mountain Cableway|gondola ski
        🚡|Aerial Tramway|cable car
        🛰️|Satellite|space orbit
        🚀|Rocket|launch space nasa moon
        🛸|Flying Saucer|ufo alien
        🛎️|Bellhop Bell|hotel service ding
        🧳|Luggage|suitcase travel trip
        ⌛|Hourglass Done|time wait
        ⏳|Hourglass Not Done|timer loading
        ⌚|Watch|clock time
        ⏰|Alarm Clock|wake time morning
        ⏱️|Stopwatch|timer race
        ⏲️|Timer Clock|cook timer
        🕰️|Mantelpiece Clock|time
        🕛|Twelve O'Clock|noon midnight
        🕧|Twelve-Thirty|time
        🕐|One O'Clock|time
        🕑|Two O'Clock|time
        🕒|Three O'Clock|time
        🕓|Four O'Clock|time
        🕔|Five O'Clock|time
        🕕|Six O'Clock|time
        🕖|Seven O'Clock|time
        🕗|Eight O'Clock|time
        🕘|Nine O'Clock|time
        🕙|Ten O'Clock|time
        🕚|Eleven O'Clock|time
        """
    )

    private val activitiesEmojis: List<EmojiItem> = parseItems(
        """
        🎃|Jack-O-Lantern|halloween pumpkin
        🎄|Christmas Tree|xmas holiday
        🎆|Fireworks|new year celebrate
        🎇|Sparkler|celebrate
        🧨|Firecracker|boom party
        ✨|Sparkles|magic stars
        🎈|Balloon|birthday party
        🎉|Party Popper|congrats celebrate hooray
        🎊|Confetti Ball|celebrate party
        🎋|Tanabata Tree|bamboo wish
        🎍|Pine Decoration|new year japan
        🎎|Japanese Dolls|festival
        🎏|Carp Streamer|koi
        🎐|Wind Chime|summer japan
        🎑|Moon Viewing Ceremony|moon
        🧧|Red Envelope|lunar gift money
        🎀|Ribbon|bow pink cute
        🎁|Wrapped Gift|present birthday
        🎗️|Reminder Ribbon|awareness
        🎟️|Admission Tickets|event concert movie
        🎫|Ticket|pass event
        🎖️|Military Medal|honor
        🏆|Trophy|win champion award 1st
        🏅|Sports Medal|winner
        🥇|1st Place Medal|gold win
        🥈|2nd Place Medal|silver
        🥉|3rd Place Medal|bronze
        ⚽|Soccer Ball|football goal
        ⚾|Baseball|sport bat
        🥎|Softball|sport
        🏀|Basketball|hoops nba
        🏐|Volleyball|beach sport
        🏈|American Football|nfl touchdown
        🏉|Rugby Football|sport
        🎾|Tennis|racket ball
        🥏|Flying Disc|frisbee
        🎳|Bowling|strike pins
        🏏|Cricket Game|bat ball
        🏑|Field Hockey|stick
        🏒|Ice Hockey|puck nhl
        🥍|Lacrosse|stick
        🏓|Ping Pong|table tennis
        🏸|Badminton|shuttlecock
        🥊|Boxing Glove|fight punch
        🥋|Martial Arts Uniform|karate judo taekwondo
        🥅|Goal Net|hockey soccer
        ⛳|Flag in Hole|golf putt
        ⛸️|Ice Skate|winter rink
        🎣|Fishing Pole|fish catch
        🤿|Diving Mask|snorkel scuba
        🎽|Running Shirt|marathon track
        🎿|Skis|snow winter
        🛷|Sled|snow toboggan
        🥌|Curling Stone|ice winter
        🎯|Bullseye|target dart goal
        🪀|Yo-Yo|toy
        🪁|Kite|wind fly
        🔫|Water Pistol|squirt gun toy
        🎱|Pool 8 Ball|billiards magic 8
        🔮|Crystal Ball|future fortune magic
        🪄|Magic Wand|wizard spell
        🎮|Video Game|controller gaming xbox ps5
        🕹️|Joystick|arcade retro game
        🎰|Slot Machine|casino jackpot 777
        🎲|Game Die|dice board game rng
        🧩|Puzzle Piece|jigsaw solve
        🧸|Teddy Bear|plush toy cute
        🪅|Piñata|party candy
        🪩|Mirror Ball|disco party dance
        🪆|Nesting Dolls|matryoshka
        ♠️|Spade Suit|cards poker
        ♥️|Heart Suit|cards poker
        ♦️|Diamond Suit|cards poker
        ♣️|Club Suit|cards poker
        ♟️|Chess Pawn|chess strategy
        🃏|Joker|card wild
        🀄|Mahjong Red Dragon|tile game
        🎴|Flower Playing Cards|hanafuda
        🎭|Performing Arts|theater drama comedy
        🖼️|Framed Picture|art museum photo
        🎨|Artist Palette|paint draw art color
        🧵|Thread|sew spool
        🪡|Sewing Needle|stitch
        🧶|Yarn|knit crochet
        🪢|Knot|rope tie
        """
    )

    private val objectsEmojis: List<EmojiItem> = parseItems(
        """
        👓|Glasses|eyeglasses read
        🕶️|Sunglasses|cool shades
        🥽|Goggles|swim lab ski
        🥼|Lab Coat|doctor scientist
        🦺|Safety Vest|construction hi-vis
        👔|Necktie|suit work formal
        👕|T-Shirt|clothes casual
        👖|Jeans|pants denim
        🧣|Scarf|winter warm
        🧤|Gloves|winter hands
        🧥|Coat|jacket winter
        🧦|Socks|feet warm
        👗|Dress|fashion outfit
        👘|Kimono|japanese traditional
        🥻|Sari|indian outfit
        🩱|One-Piece Swimsuit|swim pool
        🩲|Briefs|underwear swim
        🩳|Shorts|summer pants
        👙|Bikini|beach swim
        👚|Woman's Clothes|shirt blouse
        🪭|Folding Hand Fan|cool breeze
        👛|Purse|coin bag
        👜|Handbag|purse fashion
        👝|Clutch Bag|pouch
        🛍️|Shopping Bags|shop retail mall
        🎒|Backpack|school travel bag
        🩴|Thong Sandal|flip flop beach
        👞|Man's Shoe|dress shoe
        👟|Running Shoe|sneaker kicks
        🥾|Hiking Boot|outdoor boot
        🥿|Flat Shoe|ballet shoe
        👠|High-Heeled Shoe|heels fashion
        👡|Woman's Sandal|summer shoe
        🩰|Ballet Shoes|dance
        👢|Woman's Boot|winter boot
        🪮|Hair Pick|comb hair
        👑|Crown|king queen royal win
        👒|Woman's Hat|sun hat
        🎩|Top Hat|magic formal
        🎓|Graduation Cap|school degree grad
        🧢|Billed Cap|baseball hat cap
        🪖|Military Helmet|army soldier
        ⛑️|Rescue Worker's Helmet|safety medic
        📿|Prayer Beads|beads
        💄|Lipstick|makeup beauty
        💍|Ring|engaged wedding diamond
        💎|Gem Stone|diamond jewel crystal
        🔇|Muted Speaker|silent mute off
        🔈|Speaker Low Volume|sound audio
        🔉|Speaker Medium Volume|sound audio
        🔊|Speaker High Volume|loud audio
        📢|Loudspeaker|announce megaphone
        📣|Megaphone|cheer announce
        📯|Postal Horn|horn
        🔔|Bell|notify alert ring
        🔕|Bell with Slash|mute silent
        🎼|Musical Score|notes music
        🎵|Musical Note|song audio tune
        🎶|Musical Notes|music song dance
        🎙️|Studio Microphone|podcast record
        🎚️|Level Slider|audio mix
        🎛️|Control Knobs|dj synth audio
        🎤|Microphone|sing karaoke voice
        🎧|Headphone|music listen earbuds
        📻|Radio|fm music
        🎷|Saxophone|jazz music
        🪗|Accordion|music folk
        🎸|Guitar|rock music band
        🎹|Musical Keyboard|piano synth
        🎺|Trumpet|brass horn
        🎻|Violin|classical string
        🪕|Banjo|country music
        🥁|Drum|beat percussion
        🪘|Long Drum|conga beat
        🪇|Maracas|shake party
        🪈|Flute|wind music
        📱|Mobile Phone|iphone android smartphone
        📲|Mobile Phone with Arrow|call transfer
        ☎️|Telephone|call landline
        📞|Telephone Receiver|phone call
        📟|Pager|beeper retro
        📠|Fax Machine|office
        🔋|Battery|power charge full
        🪫|Low Battery|dead low power
        🔌|Electric Plug|power cord
        💻|Laptop|computer macbook pc code
        🖥️|Desktop Computer|imac monitor screen
        🖨️|Printer|print paper
        ⌨️|Keyboard|type vetro keys
        🖱️|Computer Mouse|click pc
        🖲️|Trackball|mouse
        💽|Computer Disk|minidisc
        💾|Floppy Disk|save retro
        💿|Optical Disk|cd dvd
        📀|DVD|movie disc
        🧮|Abacus|math count
        🎥|Movie Camera|film cinema
        🎞️|Film Frames|movie
        📽️|Film Projector|cinema
        🎬|Clapper Board|action movie film
        📺|Television|tv watch show
        📷|Camera|photo snap
        📸|Camera with Flash|photo flash
        📹|Video Camera|record video
        📼|Videocassette|vhs retro
        🔍|Magnifying Glass Tilted Left|search find zoom
        🔎|Magnifying Glass Tilted Right|search zoom
        🕯️|Candle|flame wax light
        💡|Light Bulb|idea smart bright
        🔦|Flashlight|torch dark
        🏮|Red Paper Lantern|izakaya lantern
        🪔|Diya Lamp|diwali light
        📔|Notebook with Decorative Cover|journal
        📕|Closed Book|book red
        📖|Open Book|read study story
        📗|Green Book|book
        📘|Blue Book|book
        📙|Orange Book|book
        📚|Books|library study stack
        📓|Notebook|notes journal
        📒|Ledger|notes finance
        📃|Page with Curl|document
        📜|Scroll|ancient history decree
        📄|Page Facing Up|document file
        📰|Newspaper|news press
        🗞️|Rolled-Up Newspaper|news
        📑|Bookmark Tabs|bookmark organize
        🔖|Bookmark|save read
        🏷️|Label|tag price
        💰|Money Bag|rich cash bank
        🪙|Coin|money gold
        💴|Yen Banknote|jpy money
        💵|Dollar Banknote|usd cash money
        💶|Euro Banknote|eur money
        💷|Pound Banknote|gbp money
        💸|Money with Wings|spend paid broke
        💳|Credit Card|pay buy card
        🧾|Receipt|bill invoice
        💹|Chart Increasing with Yen|stocks market
        ✉️|Envelope|email mail letter
        📧|E-Mail|email message
        📨|Incoming Envelope|mail inbox
        📩|Envelope with Arrow|sent email
        📤|Outbox Tray|upload send
        📥|Inbox Tray|download inbox
        📦|Package|box delivery amazon
        📫|Closed Mailbox with Raised Flag|mail
        📪|Closed Mailbox with Lowered Flag|mail
        📬|Open Mailbox with Raised Flag|mail
        📭|Open Mailbox with Lowered Flag|mail
        📮|Postbox|mail letter
        🗳️|Ballot Box with Ballot|vote election
        ✏️|Pencil|write draw edit
        ✒️|Black Nib|fountain pen ink
        🖋️|Fountain Pen|write sign
        🖊️|Pen|ballpoint write
        🖌️|Paintbrush|art paint
        🖍️|Crayon|draw color kids
        📝|Memo|note write list
        💼|Briefcase|work business job
        📁|File Folder|directory files
        📂|Open File Folder|files open
        🗂️|Card Index Dividers|organize tabs
        📅|Calendar|date event schedule
        📆|Tear-Off Calendar|date day
        🗒️|Spiral Notepad|notes memo
        🗓️|Spiral Calendar|schedule
        📇|Card Index|contacts rolodex
        📈|Chart Increasing|growth stonks up
        📉|Chart Decreasing|down crash loss
        📊|Bar Chart|stats data graph
        📋|Clipboard|paste copy list
        📌|Pushpin|pin save tack
        📍|Round Pushpin|location map pin
        📎|Paperclip|attach file
        🖇️|Linked Paperclips|attach
        📏|Straight Ruler|measure
        📐|Triangular Ruler|math geometry
        ✂️|Scissors|cut trim
        🗃️|Card File Box|archive
        🗄️|File Cabinet|archive files
        🗑️|Wastebasket|trash delete bin
        🔒|Locked|secure private lock
        🔓|Unlocked|open unlock
        🔏|Locked with Pen|privacy sign
        🔐|Locked with Key|security
        🔑|Key|password unlock access
        🗝️|Old Key|vintage clue
        🔨|Hammer|build tool fix
        🪓|Axe|chop wood
        ⛏️|Pick|mine craft
        ⚒️|Hammer and Pick|tools
        🛠️|Hammer and Wrench|settings fix tools
        🗡️|Dagger|knife rpg
        ⚔️|Crossed Swords|battle fight duel
        💣|Bomb|boom explode
        🪃|Boomerang|throw return
        🏹|Bow and Arrow|archery target
        🛡️|Shield|protect defend guard
        🪚|Carpentry Saw|wood tool
        🔧|Wrench|fix tool settings
        🪛|Screwdriver|tool repair
        🔩|Nut and Bolt|hardware
        ⚙️|Gear|settings config cog
        🗜️|Clamp|tool vise
        ⚖️|Balance Scale|justice law weight
        🦯|White Cane|accessibility
        🔗|Link|url chain connect
        ⛓️|Chains|lock link
        ⛓️‍💥|Broken Chain|free break
        🧰|Toolbox|repair tools
        🧲|Magnet|attract physics
        🪜|Ladder|climb up
        ⚗️|Alembic|chemistry potion
        🧪|Test Tube|science lab experiment
        🧫|Petri Dish|biology science
        🧬|DNA|genetics science life
        🔬|Microscope|science zoom
        🔭|Telescope|space stars look
        📡|Satellite Antenna|signal wifi dish
        💉|Syringe|shot vaccine medical
        🩸|Drop of Blood|donate medical
        💊|Pill|medicine health
        🩹|Adhesive Bandage|bandaid heal
        🩼|Crutch|injured
        🩺|Stethoscope|doctor health
        🩻|X-Ray|bone medical
        🚪|Door|exit enter room
        🛗|Elevator|lift floor
        🪞|Mirror|reflect look
        🪟|Window|glass view
        🛏️|Bed|sleep hotel rest
        🛋️|Couch and Lamp|sofa relax lounge
        🪑|Chair|sit seat
        🚽|Toilet|bathroom wc
        🪠|Plunger|plumbing
        🚿|Shower|bath clean water
        🛁|Bathtub|bath bubbles relax
        🪤|Mouse Trap|trap
        🪒|Razor|shave
        🧴|Lotion Bottle|soap cream sunscreen
        🧷|Safety Pin|pin baby
        🧹|Broom|sweep clean witch
        🧺|Basket|laundry picnic
        🧻|Roll of Paper|toilet paper
        🪣|Bucket|pail water
        🧼|Soap|wash bubbles clean
        🫧|Bubbles|soap float air
        🪥|Toothbrush|brush teeth clean
        🧽|Sponge|clean scrub
        🧯|Fire Extinguisher|safety emergency
        🛒|Shopping Cart|buy checkout store
        🚬|Cigarette|smoke
        ⚰️|Coffin|rip funeral
        🪦|Headstone|rip grave halloween
        ⚱️|Funeral Urn|ashes
        🧿|Nazar Amulet|evil eye luck
        🪬|Hamsa|protection hand
        🗿|Moai|statue bruh stone
        🪧|Placard|sign protest
        🪪|Identification Card|id license badge
        """
    )

    private val symbolsEmojis: List<EmojiItem> = parseItems(
        """
        🏧|ATM Sign|cash bank
        🚮|Litter in Bin Sign|trash recycle
        🚰|Potable Water|drink water
        ♿|Wheelchair Symbol|accessible
        🚹|Men's Room|restroom
        🚺|Women's Room|restroom
        🚻|Restroom|bathroom wc
        🚼|Baby Symbol|nursery
        🚾|Water Closet|toilet
        🛂|Passport Control|travel border
        🛃|Customs|airport bag
        🛄|Baggage Claim|suitcase
        🛅|Left Luggage|locker
        ⚠️|Warning|caution alert danger
        🚸|Children Crossing|school slow
        ⛔|No Entry|stop block
        🚫|Prohibited|no ban forbid
        🚳|No Bicycles|no bike
        🚭|No Smoking|no smoke
        🚯|No Littering|no trash
        🚱|Non-Potable Water|no drink
        🚷|No Pedestrians|no walk
        📵|No Mobile Phones|no phone silent
        🔞|No One Under Eighteen|18+ adult
        ☢️|Radioactive|nuclear danger
        ☣️|Biohazard|toxic danger
        ⬆️|Up Arrow|north up
        ↗️|Up-Right Arrow|northeast
        ➡️|Right Arrow|east next
        ↘️|Down-Right Arrow|southeast
        ⬇️|Down Arrow|south down
        ↙️|Down-Left Arrow|southwest
        ⬅️|Left Arrow|west back
        ↖️|Up-Left Arrow|northwest
        ↕️|Up-Down Arrow|vertical resize
        ↔️|Left-Right Arrow|horizontal swap
        ↩️|Right Arrow Curving Left|reply undo return
        ↪️|Left Arrow Curving Right|forward redo
        ⤴️|Right Arrow Curving Up|curve up
        ⤵️|Right Arrow Curving Down|curve down
        🔃|Clockwise Vertical Arrows|refresh reload
        🔄|Counterclockwise Arrows Button|sync reload refresh
        🔙|BACK Arrow|back return
        🔚|END Arrow|end finish
        🔛|ON! Arrow|on active
        🔜|SOON Arrow|soon
        🔝|TOP Arrow|top best
        🛐|Place of Worship|pray religion
        ⚛️|Atom Symbol|science physics
        🕉️|Om|hindu yoga peace
        ✡️|Star of David|jewish
        ☸️|Wheel of Dharma|buddhist
        ☯️|Yin Yang|balance harmony
        ✝️|Latin Cross|christian
        ☦️|Orthodox Cross|cross
        ☪️|Star and Crescent|islam
        ☮️|Peace Symbol|peace hippie
        🕎|Menorah|hanukkah
        🔯|Dotted Six-Pointed Star|star
        🪯|Khanda|sikh
        ♈|Aries|zodiac ram
        ♉|Taurus|zodiac bull
        ♊|Gemini|zodiac twins
        ♋|Cancer|zodiac crab
        ♌|Leo|zodiac lion
        ♍|Virgo|zodiac
        ♎|Libra|zodiac scales
        ♏|Scorpio|zodiac
        ♐|Sagittarius|zodiac bow
        ♑|Capricorn|zodiac goat
        ♒|Aquarius|zodiac
        ♓|Pisces|zodiac fish
        ⛎|Ophiuchus|zodiac
        🔀|Shuffle Tracks Button|random mix
        🔁|Repeat Button|loop repeat
        🔂|Repeat Single Button|loop one
        ▶️|Play Button|start play video
        ⏩|Fast-Forward Button|skip fast
        ⏭️|Next Track Button|skip next
        ⏯️|Play or Pause Button|play pause
        ◀️|Reverse Button|back previous
        ⏪|Fast Reverse Button|rewind
        ⏮️|Last Track Button|previous
        🔼|Upwards Button|up
        ⏫|Fast Up Button|up
        🔽|Downwards Button|down
        ⏬|Fast Down Button|down
        ⏸️|Pause Button|pause wait
        ⏹️|Stop Button|stop
        ⏺️|Record Button|record mic
        ⏏️|Eject Button|eject
        🎦|Cinema|movie film
        🔅|Dim Button|brightness low
        🔆|Bright Button|brightness high
        📶|Antenna Bars|signal wifi bars
        🛜|Wireless|wifi internet
        📳|Vibration Mode|vibrate phone
        📴|Mobile Phone Off|silent off
        ♀️|Female Sign|woman girl
        ♂️|Male Sign|man boy
        ⚧️|Transgender Symbol|trans
        ✖️|Multiply|times x math
        ➕|Plus|add plus math
        ➖|Minus|subtract dash math
        ➗|Divide|division math
        🟰|Heavy Equals Sign|equal math
        ♾️|Infinity|forever loop
        ‼️|Double Exclamation Mark|bang alert
        ⁉️|Exclamation Question Mark|what really
        ❓|Red Question Mark|what question
        ❔|White Question Mark|question
        ❕|White Exclamation Mark|alert
        ❗|Red Exclamation Mark|important alert
        〰️|Wavy Dash|wave
        💱|Currency Exchange|forex money
        💲|Heavy Dollar Sign|money cash usd
        ⚕️|Medical Symbol|doctor health
        ♻️|Recycling Symbol|recycle green eco
        ⚜️|Fleur-de-lis|french scout
        🔱|Trident Emblem|poseidon ocean
        📛|Name Badge|tag tofu
        🔰|Japanese Symbol for Beginner|newbie leaf
        ⭕|Hollow Red Circle|circle yes o
        ✅|Check Mark Button|done yes verify pass
        ☑️|Check Box with Check|done checked
        ✔️|Check Mark|yes ok tick
        ❌|Cross Mark|no wrong x close
        ❎|Cross Mark Button|no close
        ➰|Curly Loop|loop
        ➿|Double Curly Loop|voicemail
        〽️|Part Alternation Mark|wave
        ✳️|Eight-Spoked Asterisk|star
        ✴️|Eight-Pointed Star|star
        ❇️|Sparkle|green star
        ©️|Copyright|legal
        ®️|Registered|trademark
        ™️|Trade Mark|tm brand
        #️⃣|Keycap Number Sign|hashtag pound
        *️⃣|Keycap Asterisk|star
        0️⃣|Keycap 0|zero number
        1️⃣|Keycap 1|one number
        2️⃣|Keycap 2|two number
        3️⃣|Keycap 3|three number
        4️⃣|Keycap 4|four number
        5️⃣|Keycap 5|five number
        6️⃣|Keycap 6|six number
        7️⃣|Keycap 7|seven number
        8️⃣|Keycap 8|eight number
        9️⃣|Keycap 9|nine number
        🔟|Keycap 10|ten number
        🔠|Input Latin Uppercase|abc caps
        🔡|Input Latin Lowercase|abc
        🔢|Input Numbers|1234 numbers
        🔣|Input Symbols|symbols
        🔤|Input Latin Letters|abc letters
        🅰️|A Button|blood a
        🆎|AB Button|blood ab
        🅱️|B Button|blood b
        🆑|CL Button|clear
        🆒|COOL Button|cool nice
        🆓|FREE Button|free
        ℹ️|Information|info help
        🆔|ID Button|identity id
        Ⓜ️|Circled M|metro m
        🆕|NEW Button|new fresh
        🆖|NG Button|no good
        🅾️|O Button|blood o
        🆗|OK Button|okay ok
        🅿️|P Button|parking p
        🆘|SOS Button|help emergency sos
        🆙|UP! Button|up level
        🆚|VS Button|versus fight
        🔴|Red Circle|red dot
        🟠|Orange Circle|orange dot
        🟡|Yellow Circle|yellow dot
        🟢|Green Circle|green dot online
        🔵|Blue Circle|blue dot
        🟣|Purple Circle|purple dot
        🟤|Brown Circle|brown dot
        ⚫|Black Circle|black dot
        ⚪|White Circle|white dot
        🟥|Red Square|red box
        🟧|Orange Square|orange box
        🟨|Yellow Square|yellow box
        🟩|Green Square|green box
        🟦|Blue Square|blue box
        🟪|Purple Square|purple box
        🟫|Brown Square|brown box
        ⬛|Black Large Square|black box
        ⬜|White Large Square|white box
        🔶|Large Orange Diamond|diamond
        🔷|Large Blue Diamond|diamond
        🔸|Small Orange Diamond|diamond
        🔹|Small Blue Diamond|diamond
        🔺|Red Triangle Pointed Up|up triangle
        🔻|Red Triangle Pointed Down|down triangle
        💠|Diamond with a Dot|cute jewel
        🔘|Radio Button|circle target
        🔳|White Square Button|box
        🔲|Black Square Button|box
        """
    )

    private val specialFlags: List<EmojiItem> = parseItems(
        """
        🏁|Chequered Flag|finish race start
        🚩|Triangular Flag|red flag warning
        🎌|Crossed Flags|japan celebration
        🏴|Black Flag|flag
        🏳️|White Flag|surrender peace
        🏳️‍🌈|Rainbow Flag|pride lgbtq
        🏳️‍⚧️|Transgender Flag|pride trans
        🏴‍☠️|Pirate Flag|jolly roger skull
        🇺🇳|United Nations|un world flag
        🇪🇺|European Union|eu europe flag
        """
    )

    private val countryPairs: List<Pair<String, String>> = listOf(
        "AF" to "Afghanistan", "AX" to "Aland Islands", "AL" to "Albania", "DZ" to "Algeria",
        "AS" to "American Samoa", "AD" to "Andorra", "AO" to "Angola", "AI" to "Anguilla",
        "AQ" to "Antarctica", "AG" to "Antigua & Barbuda", "AR" to "Argentina", "AM" to "Armenia",
        "AW" to "Aruba", "AU" to "Australia", "AT" to "Austria", "AZ" to "Azerbaijan",
        "BS" to "Bahamas", "BH" to "Bahrain", "BD" to "Bangladesh", "BB" to "Barbados",
        "BY" to "Belarus", "BE" to "Belgium", "BZ" to "Belize", "BJ" to "Benin",
        "BM" to "Bermuda", "BT" to "Bhutan", "BO" to "Bolivia", "BA" to "Bosnia & Herzegovina",
        "BW" to "Botswana", "BR" to "Brazil", "IO" to "British Indian Ocean Territory",
        "VG" to "British Virgin Islands", "BN" to "Brunei", "BG" to "Bulgaria",
        "BF" to "Burkina Faso", "BI" to "Burundi", "KH" to "Cambodia", "CM" to "Cameroon",
        "CA" to "Canada", "IC" to "Canary Islands", "CV" to "Cape Verde",
        "BQ" to "Caribbean Netherlands", "KY" to "Cayman Islands",
        "CF" to "Central African Republic", "EA" to "Ceuta & Melilla", "TD" to "Chad",
        "CL" to "Chile", "CN" to "China", "CX" to "Christmas Island", "CC" to "Cocos Islands",
        "CO" to "Colombia", "KM" to "Comoros", "CG" to "Congo Brazzaville",
        "CD" to "Congo Kinshasa", "CK" to "Cook Islands", "CR" to "Costa Rica",
        "CI" to "Cote d'Ivoire", "HR" to "Croatia", "CU" to "Cuba", "CW" to "Curacao",
        "CY" to "Cyprus", "CZ" to "Czechia", "DK" to "Denmark", "DG" to "Diego Garcia",
        "DJ" to "Djibouti", "DM" to "Dominica", "DO" to "Dominican Republic",
        "EC" to "Ecuador", "EG" to "Egypt", "SV" to "El Salvador", "GQ" to "Equatorial Guinea",
        "ER" to "Eritrea", "EE" to "Estonia", "SZ" to "Eswatini", "ET" to "Ethiopia",
        "FK" to "Falkland Islands", "FO" to "Faroe Islands", "FJ" to "Fiji", "FI" to "Finland",
        "FR" to "France", "GF" to "French Guiana", "PF" to "French Polynesia",
        "TF" to "French Southern Territories", "GA" to "Gabon", "GM" to "Gambia",
        "GE" to "Georgia", "DE" to "Germany", "GH" to "Ghana", "GI" to "Gibraltar",
        "GR" to "Greece", "GL" to "Greenland", "GD" to "Grenada", "GP" to "Guadeloupe",
        "GU" to "Guam", "GT" to "Guatemala", "GG" to "Guernsey", "GN" to "Guinea",
        "GW" to "Guinea-Bissau", "GY" to "Guyana", "HT" to "Haiti", "HN" to "Honduras",
        "HK" to "Hong Kong", "HU" to "Hungary", "IS" to "Iceland", "IN" to "India",
        "ID" to "Indonesia", "IR" to "Iran", "IQ" to "Iraq", "IE" to "Ireland",
        "IM" to "Isle of Man", "IL" to "Israel", "IT" to "Italy", "JM" to "Jamaica",
        "JP" to "Japan", "JE" to "Jersey", "JO" to "Jordan", "KZ" to "Kazakhstan",
        "KE" to "Kenya", "KI" to "Kiribati", "XK" to "Kosovo", "KW" to "Kuwait",
        "KG" to "Kyrgyzstan", "LA" to "Laos", "LV" to "Latvia", "LB" to "Lebanon",
        "LS" to "Lesotho", "LR" to "Liberia", "LY" to "Libya", "LI" to "Liechtenstein",
        "LT" to "Lithuania", "LU" to "Luxembourg", "MO" to "Macao", "MG" to "Madagascar",
        "MW" to "Malawi", "MY" to "Malaysia", "MV" to "Maldives", "ML" to "Mali",
        "MT" to "Malta", "MH" to "Marshall Islands", "MQ" to "Martinique",
        "MR" to "Mauritania", "MU" to "Mauritius", "YT" to "Mayotte", "MX" to "Mexico",
        "FM" to "Micronesia", "MD" to "Moldova", "MC" to "Monaco", "MN" to "Mongolia",
        "ME" to "Montenegro", "MS" to "Montserrat", "MA" to "Morocco", "MZ" to "Mozambique",
        "MM" to "Myanmar", "NA" to "Namibia", "NR" to "Nauru", "NP" to "Nepal",
        "NL" to "Netherlands", "NC" to "New Caledonia", "NZ" to "New Zealand",
        "NI" to "Nicaragua", "NE" to "Niger", "NG" to "Nigeria", "NU" to "Niue",
        "NF" to "Norfolk Island", "KP" to "North Korea", "MK" to "North Macedonia",
        "MP" to "Northern Mariana Islands", "NO" to "Norway", "OM" to "Oman",
        "PK" to "Pakistan", "PW" to "Palau", "PS" to "Palestine", "PA" to "Panama",
        "PG" to "Papua New Guinea", "PY" to "Paraguay", "PE" to "Peru", "PH" to "Philippines",
        "PN" to "Pitcairn Islands", "PL" to "Poland", "PT" to "Portugal", "PR" to "Puerto Rico",
        "QA" to "Qatar", "RE" to "Reunion", "RO" to "Romania", "RU" to "Russia",
        "RW" to "Rwanda", "WS" to "Samoa", "SM" to "San Marino", "ST" to "Sao Tome & Principe",
        "SA" to "Saudi Arabia", "SN" to "Senegal", "RS" to "Serbia", "SC" to "Seychelles",
        "SL" to "Sierra Leone", "SG" to "Singapore", "SX" to "Sint Maarten",
        "SK" to "Slovakia", "SI" to "Slovenia", "SB" to "Solomon Islands", "SO" to "Somalia",
        "ZA" to "South Africa", "GS" to "South Georgia & South Sandwich Islands",
        "KR" to "South Korea", "SS" to "South Sudan", "ES" to "Spain", "LK" to "Sri Lanka",
        "BL" to "St. Barthelemy", "SH" to "St. Helena", "KN" to "St. Kitts & Nevis",
        "LC" to "St. Lucia", "MF" to "St. Martin", "PM" to "St. Pierre & Miquelon",
        "VC" to "St. Vincent & Grenadines", "SD" to "Sudan", "SR" to "Suriname",
        "SJ" to "Svalbard & Jan Mayen", "SE" to "Sweden", "CH" to "Switzerland", "SY" to "Syria",
        "TW" to "Taiwan", "TJ" to "Tajikistan", "TZ" to "Tanzania", "TH" to "Thailand",
        "TL" to "Timor-Leste", "TG" to "Togo", "TK" to "Tokelau", "TO" to "Tonga",
        "TT" to "Trinidad & Tobago", "TN" to "Tunisia", "TR" to "Turkey", "TM" to "Turkmenistan",
        "TC" to "Turks & Caicos Islands", "TV" to "Tuvalu", "VI" to "U.S. Virgin Islands",
        "UG" to "Uganda", "UA" to "Ukraine", "AE" to "United Arab Emirates",
        "GB" to "United Kingdom", "US" to "United States", "UY" to "Uruguay",
        "UZ" to "Uzbekistan", "VU" to "Vanuatu", "VA" to "Vatican City", "VE" to "Venezuela",
        "VN" to "Vietnam", "WF" to "Wallis & Futuna", "EH" to "Western Sahara",
        "YE" to "Yemen", "ZM" to "Zambia", "ZW" to "Zimbabwe",
    )

    private val countryFlags: List<EmojiItem> = countryPairs.map { (code, name) ->
        val flag = flagFromIso(code)
        EmojiItem(
            emoji = flag,
            name = "Flag: $name",
            keywords = "flag $name ${code.lowercase()}".lowercase(),
        )
    }

    val categories: List<EmojiCategorySpec> = listOf(
        EmojiCategorySpec(
            id = "smileys",
            icon = "😀",
            title = "Smileys & Emotion",
            emojis = smileysEmojis,
        ),
        EmojiCategorySpec(
            id = "people",
            icon = "👋",
            title = "People & Body",
            emojis = peopleWithTone + peopleHandsNoTone,
        ),
        EmojiCategorySpec(
            id = "nature",
            icon = "🐶",
            title = "Animals & Nature",
            emojis = natureEmojis,
        ),
        EmojiCategorySpec(
            id = "food",
            icon = "🍕",
            title = "Food & Drink",
            emojis = foodEmojis,
        ),
        EmojiCategorySpec(
            id = "travel",
            icon = "✈️",
            title = "Travel & Places",
            emojis = travelEmojis,
        ),
        EmojiCategorySpec(
            id = "activities",
            icon = "⚽",
            title = "Activities",
            emojis = activitiesEmojis,
        ),
        EmojiCategorySpec(
            id = "objects",
            icon = "💡",
            title = "Objects",
            emojis = objectsEmojis,
        ),
        EmojiCategorySpec(
            id = "symbols",
            icon = "❤️",
            title = "Symbols",
            emojis = symbolsEmojis,
        ),
        EmojiCategorySpec(
            id = "flags",
            icon = "🏁",
            title = "Flags",
            emojis = specialFlags + countryFlags,
        ),
    )

    // Gboard-style Emoticons (Kaomoji) tab inside Emoji Drawer
    val emoticonGroups: List<EmoticonGroupSpec> = listOf(
        EmoticonGroupSpec(
            id = "classic",
            title = "Classic",
            items = listOf(":-)", ":)", ":D", ";)", ":P", "XD", ":-/", ":(", ":'(", ":O", "<3", "^_^", "-_-", "o_O"),
        ),
        EmoticonGroupSpec(
            id = "happy",
            title = "Happy",
            items = listOf(
                "(◕‿◕)", "(⌒‿⌒)", "(✿◠‿◠)", "(*^▽^*)", "(☆▽☆)", "(✧ω✧)", "(≧◡≦)",
                "＼(＾▽＾)／", "ヾ(＠⌒ー⌒＠)ノ", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "(๑˃̵ᴗ˂̵)و",
            ),
        ),
        EmoticonGroupSpec(
            id = "love",
            title = "Love & Hug",
            items = listOf(
                "(づ￣ ³￣)づ", "(っ´▽`)っ", "(づ｡◕‿‿◕｡)づ", "(♡˙︶˙♡)", "(´,,•ω•,,)♡",
                "(つ≧▽≦)つ", "♡(ŐωŐ人)", "(●♡∀♡)",
            ),
        ),
        EmoticonGroupSpec(
            id = "shrug",
            title = "Shrug & Flip",
            items = listOf(
                "¯\\_(ツ)_/¯", "(╯°□°)╯︵ ┻━┻", "┬─┬ノ( º _ ºノ)", "ಠ_ಠ", "(¬_¬)",
                "(☞ﾟヮﾟ)☞", "☜(ﾟヮﾟ☜)", "(•_•) ( •_•)>⌐■-■ (⌐■_■)", "(ง •̀_•́)ง",
            ),
        ),
        EmoticonGroupSpec(
            id = "sad",
            title = "Sad & Shock",
            items = listOf(
                "(╥_╥)", "(；一_一)", "(・_・;)", "(⊙_⊙;)", "Σ(°△°|||)",
                "(>_<)", "(；´Д`)", "(-_-;)", "(つ﹏⊂)",
            ),
        ),
    )

    private val allEmojisByGlyph: Map<String, EmojiItem> by lazy {
        categories.asSequence()
            .flatMap { it.emojis.asSequence() }
            .associateBy { it.emoji }
    }

    val totalCount: Int by lazy {
        categories.sumOf { it.emojis.size }
    }

    fun findByGlyph(emoji: String): EmojiItem? = allEmojisByGlyph[emoji]

    fun search(query: String, limit: Int = 120): List<EmojiItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return categories.first().emojis
        val results = mutableListOf<EmojiItem>()
        val seen = HashSet<String>()
        for (cat in categories) {
            for (item in cat.emojis) {
                if ((item.emoji == query || item.name.lowercase().contains(q) || item.keywords.contains(q)) && seen.add(item.emoji)) {
                    results.add(item)
                    if (results.size >= limit) return results
                }
            }
        }
        return results
    }
}
