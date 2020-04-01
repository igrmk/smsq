package main

type parseKind int

const (
	parseRaw parseKind = iota
	parseHTML
	parseMarkdown
)

func (r parseKind) String() string {
	switch r {
	case parseRaw:
		return "raw"
	case parseHTML:
		return "html"
	case parseMarkdown:
		return "markdown"
	}
	return "unknown"
}
