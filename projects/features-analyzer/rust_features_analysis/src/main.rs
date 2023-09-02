// use regex::Regex

use std::fs::File;
use std::io::{BufRead, BufReader, Error, ErrorKind};
use std::path::Path;
use regex::Regex;
fn main() -> Result<(), Error> {


    let path_to_read = Path::new("./features_list.txt");

    let regex = Regex::new(r"feature\([[:word:]]+\)").unwrap();


    let file = File::open(&path_to_read).expect("Unable to open file");
    let file = BufReader::new(file);
    for line in file.lines() {
        // result = regex.find_iter(text).map(|mat| mat.as_str()).collect()
        let s = line.unwrap();
        let caps = regex.captures(&s);
        if caps == Option(Ok) {
            println!("caps: {}", &caps[0])
        }
    }
    Ok(())
}
