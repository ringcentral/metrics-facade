echo "Process chapters/01-first-creation-and-export.md"
plantuml -tsvg chapters/01-first-creation-and-export.md -o svgs/01-first-creation-and-export
echo "Process chapters/02-metric-types.md"
plantuml -tsvg chapters/02-metric-types.md -o svgs/02-metric-types
echo "Process chapters/03-labeled-metrics.md"
plantuml -tsvg chapters/03-labeled-metrics.md -o svgs/03-labeled-metrics
echo "Process chapters/04-configuration.md"
plantuml -tsvg chapters/04-configuration.md -o svgs/04-configuration