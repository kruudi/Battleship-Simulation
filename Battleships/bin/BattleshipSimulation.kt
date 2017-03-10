/**
 * --------------------------------------------------------------------
 * ----------------------- BATTLESHIP SIMULATION ----------------------
 * --------------------------------------------------------------------
 *
 *                   [ ] [*] [ ] [ ] [ ]   <- YOU
 *
 *                                V        <- Guided torpedo
 *
 *                   [ ] [ ] [ ] [*] [ ]   <- ENEMY
 *
 *
 * You are tasked to write an AI for the battleship game simulation.
 * Each player has four battleships [] and one flagship [*] at random
 * places in a row. The goal is to guess the location of the opponent's
 * flagship and hit it with a guided torpedo, which can be fired once
 * from any ship. After torpedo's target is locked, its course can only
 * be slightly corrected. The battle is simulated many times and the
 * player with a better win ratio is the ultimate winner.
 *
 * The enemy has already overridden the Command Center and is now
 * stably winning the simulation. Can you find enemy's weak spots
 * and write an even better Command Center? Desperate times require
 * new ideas, so we are using Kotlin now. Place your code only inside
 * the `YourCommandCenter` class located at the bottom of this file.
 * Please document your logic so that we can understand not only how,
 * but also *why* it works.
 *
 * Send your solution to careers@icefire.ee
 */

val random = java.util.Random()

data class TorpedoAttack(val source: Int, val target: Int)

// CommandCenter interface with the default behaviour
interface CommandCenter {

  fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
    // Flagship is marked as Boolean TRUE
    val flagship = ships.indexOfFirst { it }
    // Fire torpedo from flagship at random target
    return TorpedoAttack(flagship, random.nextInt(5))
  }

  fun guideTorpedo(attack: TorpedoAttack): Int {
    // Return either -1, 0, 1 to move torpedo a bit
    return random.nextInt(3) - 1
  }

  fun onTorpedoDetected(attack: TorpedoAttack) {
    // Triggered when the opponent fires his torpedo
  }

}

fun simulate(): Pair<Boolean, Boolean>  {
  // Initialize command centers
  val yourCC = YourCommandCenter()
  val enemyCC = EnemyCommandCenter()

  // Initialize battleships
  val yourShips = BooleanArray(5)
  val enemyShips = BooleanArray(5)

  // Randomly place flagships
  yourShips[random.nextInt(yourShips.size)] = true
  enemyShips[random.nextInt(yourShips.size)] = true

  // Your fire first
  val yourAttack = yourCC.fireTorpedo(yourShips)
  enemyCC.onTorpedoDetected(yourAttack);

  // Enemy fires second
  val enemyAttack = enemyCC.fireTorpedo(enemyShips)
  yourCC.onTorpedoDetected(enemyAttack);

  // You guide your torpedo
  val yourTarget = yourAttack.target + yourCC.guideTorpedo(yourAttack) % 2

  // Enemy guides his torpedo
  val enemyTarget = enemyAttack.target + enemyCC.guideTorpedo(enemyAttack) % 2

  // Check if each side's flagship has been hit
  // Note: Both players can be winners at the same time
  return Pair(
          yourShips.getOrElse(enemyTarget) { false },
          enemyShips.getOrElse(yourTarget) { false }
  )
}

fun main(args: Array<String>) {

  var yourWins = 0;
  var enemyWins = 0;

  for (i in 1..100000) {
    val (enemyWon, youWon) = simulate();
    if (youWon) yourWins++;
    if (enemyWon) enemyWins++;
  }

  val ratio = yourWins.toDouble() / enemyWins.toDouble()

  println("Your wins to enemy's wins ratio is $ratio")

  if (ratio < 1.0) println("You lost! Improve your battle logic!")
  else if (ratio < 1.25) println("You won by a small margin! Can you do better?")
  else println("Congratulations! Your victory is undeniable. Good job!")
}

class EnemyCommandCenter : CommandCenter {

  var attackedShip = -1

  override fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
    // Enemy has overridden this method to hide his flagship
    val flagship = ships.indexOfFirst { it }
    var source: Int;

    do source = random.nextInt(5) while (source == flagship || source == attackedShip)

    return TorpedoAttack(source, random.nextInt(5))
  }

  override fun guideTorpedo(attack: TorpedoAttack): Int {
    // Ensure that torpedo doesn't sway off and always hits at least some ship
    return if (attack.target in 1..3) random.nextInt(3) - 1 else 0;
  }

  override fun onTorpedoDetected(attack: TorpedoAttack) {
    attackedShip = attack.target
  }
}

class YourCommandCenter : CommandCenter {
/* I know that the enemy currently does not care about the source of my attack.
 * I also know that the enemy doesn't attack from their flagships or targeted ships.
 * Since I always go first I can only react through guiding my torpedo.
 * Simulation runs through the game only once and then starts a new one so a situation
 * where destroyed ships might want to attack cannot happen.
 * Most games have no winner.
 */
  
  //Source of the enemy's attack
  var lastSource = -1

	
  override fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
	/* Since the enemy doesn't care about the source of my attack, I don't either.
 	 * Since I want my attack to be as well guidable as possible I will not attack the edges.
 	 */
    return TorpedoAttack(random.nextInt(5), random.nextInt(3) + 1)
  }

  override fun guideTorpedo(attack: TorpedoAttack): Int {
    /* It is better to never attack the source of the enemy's attack.
	 * I should also always guide my torpedo away from my initial target
	 */
	  
	//guide value for my torpedo
	var guiding: Int;
	
	//first case: enemy's attack source is 1 higher than my target, guide lower
	if (lastSource == attack.target +1) guiding = -1
    //second case: enemy's attack source is 1 lower than my target, guide higher
	else if (lastSource == attack.target -1) guiding = 1
	//otherways guiding is random
	else if (random.nextBoolean()) guiding = -1 else guiding = 1;
	
    return guiding;
  }

  override fun onTorpedoDetected(attack: TorpedoAttack) {
	// Get the source of the enemy's attack
	lastSource = attack.source
  }
}