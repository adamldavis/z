
import org.yaml.snakeyaml.Yaml
import org.apache.commons.io.*

def map = [:]

map = (new Yaml().load(new FileReader("colors/colors1.yml")))

println map
