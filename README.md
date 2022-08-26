# NUFORC dataset scrapper

The idea for this scrapper comes from the following [repo](https://github.com/planetsig/ufo-reports) holding data from more than 8 years ago. 

## Goal

By running the scrapper, you will scrap every data ([sorted per date](https://nuforc.org/webreports/ndxevent.html)) from the [NUFORC website](https://nuforc.org/).

Data scrapped is stored inside `data.csv` file inside the project directory. If you don't want to run the scrapper, you can download the `dataset.csv` file which may be (and surely is) out of date. 

### Data structure

Data inside the CSV file has the same structure as any report webpage (for instance, this [one](https://nuforc.org/webreports/ndxe202205.html)). That is to say :

| Date / Time | City | State | Country | Shape | Duration | Summary | Posted | Images |
|-------------| ---- | ----- | ------- | ----- | -------- | ------- | ------ |--------|

## Dependencies

You will need the following requirements
- Java
- Scala build tool (sbt)

## Getting started

Open a terminal, clone the repository and run sbt
```bash
cd <project folder>
sbt run
```
You should now have a `data.csv` file inside `<project folder>`.

## Future Work

Currently, latitude and longitude of the reports are missing. They need to be extrapolated from the city of the report. 
