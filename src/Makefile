report:
	java -cp .:chemotaxis/org.json.jar chemotaxis.sim.Simulator --team s2 -r 500 -b 2 -m 50x50ZigZag.map -s 10 -l log.txt

verify:
	java -cp .:chemotaxis/org.json.jar chemotaxis.sim.Simulator --team s1 -m simple25.map -c -v -l log.txt

gui:
	java -cp .:chemotaxis/org.json.jar chemotaxis.sim.Simulator --team s2 --gui -l log.txt

compile:
	javac -cp .:chemotaxis/org.json.jar chemotaxis/*/*.java

clean:
	rm chemotaxis/*/*.class
