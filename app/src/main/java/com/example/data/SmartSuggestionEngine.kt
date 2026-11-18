package com.example.data

import com.example.model.SmartSuggestion

object SmartSuggestionEngine {

    // Common sentence starters when starting from blank or after terminal punctuation
    private val defaultSentenceStarters = listOf("I", "The", "How")

    // Expansive Markov N-Gram next-word predictions enabling continuous Gboard-style sentence drafting
    private val nextWordPredictions: Map<String, List<String>> = mapOf(
        "i" to listOf("am", "have", "think", "will", "would", "want", "can"),
        "i'm" to listOf("going to", "so glad", "on my way", "not sure", "thinking"),
        "i've" to listOf("been", "seen", "done", "got", "had"),
        "i'll" to listOf("be there", "call you", "check it", "let you know"),
        "am" to listOf("going to", "ready", "so happy", "wondering", "working on"),
        "have" to listOf("a great", "to say", "been", "seen", "done", "no idea"),
        "think" to listOf("that", "it is", "about", "we should", "so"),
        "will" to listOf("be", "have", "do", "call", "send", "let"),
        "would" to listOf("be", "like", "love to", "prefer", "have"),
        "want" to listOf("to see", "to know", "to make", "you to", "more"),
        "you" to listOf("are", "can", "know", "have", "want", "will"),
        "you're" to listOf("welcome", "right", "going to", "awesome"),
        "you've" to listOf("got", "been", "done", "made"),
        "are" to listOf("you", "we", "going to", "doing", "ready"),
        "we" to listOf("can", "are", "should", "will", "have", "need to"),
        "we're" to listOf("going to", "ready", "here", "happy"),
        "we'll" to listOf("see", "be there", "talk soon", "make sure"),
        "can" to listOf("you", "we", "i", "be", "help", "do"),
        "could" to listOf("you", "we", "be", "have", "help"),
        "should" to listOf("we", "i", "be", "check", "know"),
        "what" to listOf("is", "do you", "are you", "about", "time"),
        "what's" to listOf("up", "the plan", "going on", "new"),
        "how" to listOf("are you", "is it", "about", "was", "does"),
        "where" to listOf("are you", "is the", "do we", "should we"),
        "when" to listOf("will", "can we", "do you", "is"),
        "why" to listOf("not", "did", "would", "is"),
        "who" to listOf("is", "was", "are", "can"),
        "that" to listOf("sounds", "is", "was", "would be", "makes sense"),
        "this" to listOf("is", "looks", "sounds", "was", "weekend"),
        "it" to listOf("is", "was", "looks", "sounds", "would be", "works"),
        "it's" to listOf("going to", "so good", "working", "great", "done"),
        "there" to listOf("is", "are", "was", "will be"),
        "let" to listOf("me", "us", "know", "them"),
        "let's" to listOf("do it", "go", "meet", "see", "catch up"),
        "thank" to listOf("you", "you so much", "god"),
        "thanks" to listOf("for", "again", "a lot", "so much"),
        "please" to listOf("let me know", "check", "send", "help"),
        "sorry" to listOf("about that", "for the delay", "i missed you"),
        "good" to listOf("morning", "night", "luck", "idea", "to see you"),
        "sounds" to listOf("good", "great", "awesome", "like a plan", "fun"),
        "looks" to listOf("great", "good", "amazing", "clean"),
        "going" to listOf("to", "home", "out", "well"),
        "to" to listOf("be", "do", "see", "get", "have", "know", "make"),
        "be" to listOf("there", "able to", "great", "careful", "ready"),
        "do" to listOf("you", "it", "that", "this", "something"),
        "make" to listOf("sure", "it", "sense", "time"),
        "get" to listOf("it", "back to", "ready", "started"),
        "know" to listOf("what", "if", "how", "about", "more"),
        "see" to listOf("you", "if", "what", "how"),
        "call" to listOf("you", "me", "it", "back"),
        "send" to listOf("me", "it", "the", "over"),
        "check" to listOf("it out", "the", "in", "with"),
        "help" to listOf("you", "with", "me"),
        "meet" to listOf("up", "you", "at", "tomorrow"),
        "for" to listOf("you", "the", "sure", "now", "me"),
        "with" to listOf("you", "me", "that", "the"),
        "on" to listOf("my way", "the", "it", "time", "Monday"),
        "at" to listOf("the", "all", "home", "work", "noon"),
        "in" to listOf("the", "a minute", "touch", "case"),
        "about" to listOf("it", "that", "the", "time"),
        "so" to listOf("good", "much", "happy", "excited", "far"),
        "just" to listOf("wanted to", "letting you know", "checking in", "got here"),
        "not" to listOf("sure", "yet", "at all", "now", "bad"),
        "if" to listOf("you", "we", "it", "that"),
        "because" to listOf("i", "it", "we", "of"),
        "the" to listOf("best", "way", "plan", "time", "first", "next"),
        "my" to listOf("way", "friend", "phone", "email", "pleasure"),
        "your" to listOf("time", "help", "message", "call"),
        "our" to listOf("team", "meeting", "way", "project"),
        "their" to listOf("way", "team", "project"),
        "all" to listOf("good", "right", "set", "day", "the best"),
        "always" to listOf("here", "good", "happy to"),
        "never" to listOf("mind", "give up", "knew"),
        "really" to listOf("good", "great", "appreciate it", "cool"),
        "liquid" to listOf("glass", "frost", "flow", "animation"),
        "glass" to listOf("keyboard", "pill", "refraction", "studio"),
        "vetro" to listOf("keyboard", "liquid glass", "studio"),
    )

    // Frequency-ordered mainstream English vocabulary for high-speed prefix matching
    private val commonWords: List<String> = (
        "the,be,to,of,and,a,in,that,have,i,it,for,not,on,with,he,as,you,do,at,this,but,his,by," +
            "from,they,we,say,her,she,or,an,will,my,one,all,would,there,their,what,so,up,out,if," +
            "about,who,get,which,go,me,when,make,can,like,time,no,just,him,know,take,people,into," +
            "year,your,good,some,could,them,see,other,than,then,now,look,only,come,its,over,think," +
            "also,back,after,use,two,how,our,work,first,well,way,even,new,want,because,any,these," +
            "give,day,most,us,is,are,was,were,been,being,has,had,did,does,doing,made,said,got," +
            "really,great,awesome,thanks,thank,please,sorry,hello,hey,morning,tonight,today," +
            "tomorrow,yesterday,maybe,always,never,often,sometimes,actually,basically,literally," +
            "probably,definitely,seriously,quickly,smooth,clean,liquid,glass,keyboard,android," +
            "iphone,gboard,emoji,symbol,character,cursor,clipboard,snippet,theme,frost,crystal," +
            "aurora,obsidian,floating,minimal,modern,design,screen,typing,message,email,search," +
            "send,done,next,code,build,create,update,change,check,select,copy,paste,delete,undo," +
            "save,pin,open,close,start,stop,right,left,down,above,below,around,between,through," +
            "before,during,while,since,until,where,why,every,everything,everyone,anything,someone," +
            "nothing,something,another,different,important,possible,available,perfect,beautiful," +
            "simple,smart,fast,easy,better,best,more,much,many,very,still,here,going,wanting," +
            "looking,working,feeling,trying,using,making,getting,having,taking,seeing,coming," +
            "knowing,thinking,should,might,must,shall,need,feel,seem,leave,call,keep,let,begin," +
            "help,talk,turn,show,hear,play,run,move,live,believe,hold,bring,happen,write,provide," +
            "sit,stand,lose,pay,meet,include,continue,set,learn,lead,understand,watch,follow," +
            "speak,read,allow,add,spend,grow,walk,win,offer,remember,love,consider,appear,buy," +
            "wait,serve,die,expect,stay,fall,cut,reach,kill,remain,suggest,raise,pass,sell," +
            "require,report,decide,pull,app,phone,text,note,notes,list,project,idea,team,friend," +
            "friends,family,home,office,place,world,life,hand,part,child,eye,woman,man,week," +
            "case,point,company,number,group,problem,fact,water,room,mother,area,money,story," +
            "month,lot,study,book,job,word,business,issue,side,kind,head,house,service,father," +
            "power,hour,game,line,end,member,law,car,city,community,name,president,minute,body," +
            "information,parent,face,others,level,door,health,person,art,war,history,party," +
            "result,reason,research,girl,guy,moment,air,teacher,force,education,happy,excited," +
            "amazing,cool,nice,fine,sure,okay,yes,yeah,nope,wait,soon,later,already,almost," +
            "enough,together,alone,ready,able,alpha,beta,gamma,delta,sum,arrow,heart,fire,degree"
        ).split(',')

    fun extractCurrentWord(textBeforeCursor: String): String {
        if (textBeforeCursor.isEmpty()) return ""
        val last = textBeforeCursor.last()
        if (!last.isLetterOrDigit() && last != '\'') return ""
        var start = textBeforeCursor.length
        while (start > 0) {
            val ch = textBeforeCursor[start - 1]
            if (ch.isLetterOrDigit() || ch == '\'') start-- else break
        }
        return textBeforeCursor.substring(start)
    }

    fun suggest(
        textBeforeCursor: String,
        learnedWords: Set<String> = emptySet(),
    ): List<SmartSuggestion> {
        val currentWord = extractCurrentWord(textBeforeCursor)

        // Case 1: Cursor is after a space or at sentence start -> Continuous Next-Word / Sentence Prediction
        if (currentWord.isEmpty()) {
            val trimmed = textBeforeCursor.trimEnd()
            if (trimmed.isEmpty() || textBeforeCursor.endsWith("\n") || trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) {
                return defaultSentenceStarters.mapIndexed { idx, s ->
                    SmartSuggestion(displayText = s, commitText = "$s ", isPrimary = idx == 0)
                }
            }

            val lastWord = extractCurrentWord(trimmed).lowercase()
            val nextList = nextWordPredictions[lastWord]
            if (!nextList.isNullOrEmpty()) {
                return nextList.take(3).mapIndexed { idx, nextWord ->
                    SmartSuggestion(
                        displayText = nextWord,
                        commitText = "$nextWord ",
                        isPrimary = idx == 0,
                    )
                }
            }

            // Fallback general connecting words so predictions NEVER abruptly stop
            return listOf("to", "and", "the").mapIndexed { idx, w ->
                SmartSuggestion(displayText = w, commitText = "$w ", isPrimary = idx == 0)
            }
        }

        // Case 2: User is actively typing letters -> Prefix Word Completion
        val lower = currentWord.lowercase()
        val isCapitalized = currentWord.first().isUpperCase()
        val isAllCaps = currentWord.length > 1 && currentWord.all { it.isUpperCase() }

        fun formatCase(word: String): String = when {
            isAllCaps -> word.uppercase()
            isCapitalized -> word.replaceFirstChar { it.uppercase() }
            else -> word
        }

        val matches = (learnedWords.asSequence() + commonWords.asSequence())
            .distinct()
            .filter { it.startsWith(lower) }
            .sortedWith(compareBy<String> { it != lower }.thenBy { it.length })
            .take(3)
            .toList()

        val results = matches.mapIndexed { idx, word ->
            val formatted = formatCase(word)
            SmartSuggestion(
                displayText = formatted,
                commitText = "$formatted ",
                isPrimary = idx == 0,
            )
        }

        if (results.isEmpty()) {
            return listOf(
                SmartSuggestion(
                    displayText = currentWord,
                    commitText = "$currentWord ",
                    isPrimary = true,
                )
            )
        }

        return results
    }
}
