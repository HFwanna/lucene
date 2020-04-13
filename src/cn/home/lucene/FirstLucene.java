package cn.home.lucene;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class FirstLucene {
	@Test
	public void testIndex() throws IOException {
		//创建一个indexwriter对象
		//文件目录
		Directory directory = FSDirectory.open(new File("G:\\BaiduNetdiskDownload\\"
				+ "luncene&solr.（77-78天）\\索引库1"));
		//词法分析器
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		//创建field对象，将field对象添加到document对象中
		File f = new File("G:\\BaiduNetdiskDownload\\luncene&solr.（77-78天）\\Lucene&solr\\01.参考资料\\searchsource");
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			//创建document对象
			Document document = new Document(); 
			//文件名称
			String file_name = file.getName();
			Field fileNameField = new TextField("fileName", file_name, Store.YES);
			//文件大小
			long file_size = FileUtils.sizeOf(file);
			Field fileSizeField = new LongField("fileSize", file_size, Store.YES);
			//文件路径
			String file_path = file.getPath();
			Field filePathField = new StoredField("filePath", file_path);
			//文件内容
			String file_content = FileUtils.readFileToString(file);
			Field fileContentField = new TextField("fileContent",file_content,Store.YES);
			
			document.add(fileNameField);
			document.add(fileSizeField);
			document.add(filePathField);
			document.add(fileContentField);
			
			//使用indexwriter对象将document对象写入索引库，此过程进行索引创建，并将索引和document对象写入索引库
			indexWriter.addDocument(document);
		}
		//关闭indexwriter对象
		indexWriter.close();
		
	}
	
	@Test
	public void testSearch() throws Exception{
		Directory directory = FSDirectory.open(new File("G:\\BaiduNetdiskDownload\\"
				+ "luncene&solr.（77-78天）\\索引库1"));
		//内存索引库
//		Directory directory2 = new RAMDirectory();
		//创建一个indexReader对象，需要指定目录
		IndexReader indexReader = DirectoryReader.open(directory);
		//创建一个indexSearcher对象，需要指定IndexReader对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//创建一个TermQuery对象，指定查询的域和查询的关键词
		Query query = new TermQuery(new Term("fileName", "java.txt"));
		//执行查询
		TopDocs topDocs = indexSearcher.search(query, 10);
		//返回查询结果，遍历查询结果并输出
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int doc = scoreDoc.doc;
			//查询出来的文档之一
			Document document = indexSearcher.doc(doc);
			String fileName = document.get("fileName");
			System.out.println(fileName);
			String fileContent = document.get("fileContent");
			System.out.println(fileContent);
			String fileSize = document.get("fileSize");
			System.out.println(fileSize);
			String filePath = document.get("filePath");
			System.out.println(filePath);
		}
		indexReader.close();
	}
	
	//查看标准分析器的分词效果
	@Test
	public void testTokenStream() throws Exception {
		//创建一个标准分析器对象
//		Analyzer analyzer = new StandardAnalyzer();
		//第三方中文分析器，这个可以扩展三个文件：扩展词文件ext.dic、IKAnalyzer.cfg.xml配置文件、stopword.dic扩展停止字典
		IKAnalyzer analyzer = new IKAnalyzer();
		
		//获得tokenStream对象
		//第一个参数：域名，可以随便给一个
		//第二个参数：要分析的文本内容
		TokenStream tokenStream = analyzer.tokenStream("test", 
				"我是ik，xwsl啊，蛋疼");
		//添加一个引用，可以获得每个关键词
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		//添加一个偏移量的引用，记录了关键词的开始位置以及结束位置
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		//将指针调整到列表的头部
		tokenStream.reset();
		//遍历关键词列表，通过incrementToken方法判断列表是否结束
		while(tokenStream.incrementToken()) {
			//关键词的起始位置
			System.out.println("start->" + offsetAttribute.startOffset());
			//取关键词
			System.out.println(charTermAttribute);
			//结束位置
			System.out.println("end->" + offsetAttribute.endOffset());
		}
		tokenStream.close();
		analyzer.close();
	}
	
	public IndexWriter getIndexWriter() throws IOException{
		//得到路径
		Directory directory = FSDirectory.open(new File("G:\\BaiduNetdiskDownload\\"
				+ "luncene&solr.（77-78天）\\索引库1"));
		//词法分析器
		Analyzer analyzer = new IKAnalyzer();
		//indexWriterConfig
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, analyzer);
		//indexWriter对象
		return new IndexWriter(directory, indexWriterConfig);
	}
	
	@Test
	//全部删除
	public void testAllDelete() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		indexWriter.deleteAll();
		indexWriter.close();
	}
	
	@Test
	//根据条件删除
	public void testDelete() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		Query query = new TermQuery(new Term("fileName", "java.txt"));
		indexWriter.deleteDocuments(query);
		indexWriter.close();
	}
	
	@Test
	//修改
	public void testUpdate() throws IOException {
		IndexWriter indexWriter = getIndexWriter();
		Document document = new Document();
		document.add(new TextField("fileN", "测试文件名",Store.YES));
		document.add(new TextField("fileC", "测试文件内容",Store.YES));
		//把pig.txt这个文档删除，并增加document文档
		indexWriter.updateDocument(new Term("fileName", "pig.txt"), document, new IKAnalyzer());
		indexWriter.close();
	}

	public IndexSearcher getIndexSearch() throws IOException{
		//得到路径
		Directory directory = FSDirectory.open(new File("G:\\BaiduNetdiskDownload\\"
				+ "luncene&solr.（77-78天）\\索引库1"));
		//创建一个indexReader对象，需要指定目录
		IndexReader indexReader = DirectoryReader.open(directory);
		//创建一个indexSearcher对象，需要指定IndexReader对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//indexWriter对象
		return indexSearcher;
	}
	
	//打印查询内容
	public void print(IndexSearcher indexSearcher,Query query) throws IOException {
		TopDocs topDocs = indexSearcher.search(query, 20);
		//打印
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int doc = scoreDoc.doc;
			Document document = indexSearcher.doc(doc);
			String fileName = document.get("fileName");
			System.out.println(fileName);
			String fileContent = document.get("fileContent");
			System.out.println(fileContent);
			String fileSize = document.get("fileSize");
			System.out.println(fileSize);
			String filePath = document.get("filePath");
			System.out.println(filePath);
		}
	}
	@Test
	//查询所有
	public void testMatchAllDocQuery() throws IOException {
		IndexSearcher indexSearcher = getIndexSearch();
		Query query = new MatchAllDocsQuery();
		print(indexSearcher, query);
		indexSearcher.getIndexReader().close();
	}
	
	@Test
	//根据数值范围查询
	public void testNumbericRangeQuery() throws IOException {
		IndexSearcher indexSearcher = getIndexSearch();
		//最后两个参数表示范围边界数字包不包含
		Query query = NumericRangeQuery.newLongRange("fileSize", 13L, 200L, true, true);
		print(indexSearcher, query);
		indexSearcher.getIndexReader().close();
	}
	
	@Test
	//条件组合查询
	public void testBooleanQuery() throws IOException {
		IndexSearcher indexSearcher = getIndexSearch();
		//最后两个参数表示范围边界数字包不包含
		BooleanQuery booleanQuery = new BooleanQuery();
		Query query1 = new TermQuery(new Term("fileName", "apache"));
		Query query2 = new TermQuery(new Term("fileName", "lucene"));
		
		//Occur.MUST 查询的文档必须有这个值才行
		booleanQuery.add(query1,Occur.MUST);
		//Occur.SHOULD相当于 或 ，可有可无
		booleanQuery.add(query2,Occur.SHOULD);
		
		print(indexSearcher, booleanQuery);
		indexSearcher.getIndexReader().close();
	}
	
	@Test
	//条件解析对象查询
	public void testQueryParse() throws IOException, ParseException {
		IndexSearcher indexSearcher = getIndexSearch();
		//设置默认查询域为fileName,指定语法分析器
		QueryParser queryParser = new QueryParser("fileName", new IKAnalyzer());
		//设置查询条件，*:*表示查询所有域和所有内容    ，如果 : 左边指定一个域那么默认域失效，右边内容会被词法分析器解析成词进行查询
//		Query query = queryParser.parse("pig.txt");
		//无法实现范围查询，因为范围查询语法：  fileSize:{47 TO 200]是通过数字确定范围，而语法解析会把内容解析成字符串，所以无法确定范围
//		Query query2 = queryParser.parse("fileSize:{47 TO 200]");
		//组合条件查询 +表示must -表示不包含条件 直接没有符号表示should
		Query query3 = queryParser.parse("+fileName:apache fileContent:apache");
		//AND OR语法等同于上面两个+fileName:apache +fileContent:apache
//		Query query3 = queryParser.parse("fileName:apache AND fileContent:apache");
		
		
		print(indexSearcher, query3);
		indexSearcher.getIndexReader().close();
	}
	
	@Test
	//条件解析对象查询-多默认域
	public void testMultiFieldQueryParse() throws IOException, ParseException {
		IndexSearcher indexSearcher = getIndexSearch();
		//设置默认查询域为fileName,fileContent,指定语法分析器
		String[] fields = {"fileName","fileContent"};
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, new IKAnalyzer());
		//表示文件名或者内容中包含java 或者 txt 其实相当于fileName:java txt AND fileContent:java txt
		//所以这种多默认域没什么卵用
		Query query3 = queryParser.parse("java txt");
		
		
		print(indexSearcher, query3);
		indexSearcher.getIndexReader().close();
	}
}
